// 말잇다 whisper.cpp JNI 브리지. 모델·실행기 모두 단말 안에서 동작하며 네트워크를 쓰지 않는다.
#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <thread>
#include "whisper.h"

#define TAG "MalitdaWhisper"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static void whisper_log_cb(enum ggml_log_level level, const char * text, void * /*user*/) {
    if (level == GGML_LOG_LEVEL_ERROR) __android_log_print(ANDROID_LOG_ERROR, TAG, "%s", text);
    // INFO/WARN 로그는 인식 문장을 포함할 수 있어 남기지 않는다.
}

extern "C" JNIEXPORT jlong JNICALL
Java_kr_voicemate_malitda_stt_WhisperNative_init(JNIEnv * env, jclass, jstring jpath) {
    whisper_log_set(whisper_log_cb, nullptr);
    const char * path = env->GetStringUTFChars(jpath, nullptr);
    whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = false;
    cparams.flash_attn = false;
    whisper_context * ctx = whisper_init_from_file_with_params(path, cparams);
    env->ReleaseStringUTFChars(jpath, path);
    if (!ctx) { LOGE("whisper_init failed"); return 0; }
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_kr_voicemate_malitda_stt_WhisperNative_release(JNIEnv *, jclass, jlong handle) {
    if (handle) whisper_free(reinterpret_cast<whisper_context *>(handle));
}

extern "C" JNIEXPORT jstring JNICALL
Java_kr_voicemate_malitda_stt_WhisperNative_version(JNIEnv * env, jclass) {
    return env->NewStringUTF(whisper_version());
}

extern "C" JNIEXPORT jstring JNICALL
Java_kr_voicemate_malitda_stt_WhisperNative_systemInfo(JNIEnv * env, jclass) {
    return env->NewStringUTF(whisper_print_system_info());
}

/**
 * 16kHz mono float PCM(-1..1) 전체를 한 번에 인식한다.
 * 반환: 세그먼트 텍스트를 공백으로 이은 문자열. 모든 세그먼트가 '무발화' 판정이면 빈 문자열.
 */
extern "C" JNIEXPORT jstring JNICALL
Java_kr_voicemate_malitda_stt_WhisperNative_transcribe(JNIEnv * env, jclass, jlong handle, jfloatArray jpcm, jstring jlang, jint nThreads, jfloat noSpeechThold) {
    whisper_context * ctx = reinterpret_cast<whisper_context *>(handle);
    if (!ctx) return env->NewStringUTF("");
    jsize n = env->GetArrayLength(jpcm);
    std::vector<float> pcm(n);
    env->GetFloatArrayRegion(jpcm, 0, n, pcm.data());
    const char * lang = env->GetStringUTFChars(jlang, nullptr);

    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.language = lang;
    params.translate = false;
    params.detect_language = false;
    params.no_context = true;
    params.no_timestamps = true;
    params.single_segment = false;
    params.print_special = false;
    params.print_progress = false;
    params.print_realtime = false;
    params.print_timestamps = false;
    params.suppress_blank = true;
    params.suppress_nst = true;
    params.temperature = 0.0f;
    params.greedy.best_of = 1;
    params.n_threads = nThreads > 0 ? nThreads : (int) std::max(1u, std::min(4u, std::thread::hardware_concurrency()));
    // 인코더는 기본적으로 30초 창(1500 위치)을 통째로 계산한다. 짧은 발화는 창을 발화 길이(+2초)에 맞춰 줄여
    // 처리시간을 크게 낮춘다(whisper.cpp stream 예제와 같은 기법). 최소 5초 창은 유지한다.
    {
        const double seconds = (double) n / WHISPER_SAMPLE_RATE;
        int ctx = (int) ((seconds + 2.0) * 50.0);   // 50 위치 = 1초
        if (ctx < 384) ctx = 384;                    // 너무 좁히면 짧은 발화에서 반복 출력이 늘어난다
        if (ctx > 1500) ctx = 1500;
        params.audio_ctx = ctx;
    }

    std::string out;
    int rc = whisper_full(ctx, params, pcm.data(), n);
    if (rc == 0) {
        const int nseg = whisper_full_n_segments(ctx);
        std::string prev;
        for (int i = 0; i < nseg; i++) {
            const float nsp = whisper_full_get_segment_no_speech_prob(ctx, i);
            if (nsp > noSpeechThold) continue;   // 무발화 확률이 높은 구간은 환각 방지를 위해 버린다
            const char * t = whisper_full_get_segment_text(ctx, i);
            if (!t) continue;
            std::string s(t);
            // 앞뒤 공백 제거
            const size_t b = s.find_first_not_of(" \t\r\n");
            if (b == std::string::npos) continue;
            const size_t e = s.find_last_not_of(" \t\r\n");
            s = s.substr(b, e - b + 1);
            // 디코더 반복 출력(직전 세그먼트와 완전히 같은 문장)은 발화가 아니므로 한 번만 남긴다
            if (s == prev) continue;
            prev = s;
            if (!out.empty()) out += ' ';
            out += s;
        }
    } else {
        LOGE("whisper_full rc=%d", rc);
    }
    env->ReleaseStringUTFChars(jlang, lang);
    return env->NewStringUTF(out.c_str());
}
