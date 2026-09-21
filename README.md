# 말잇다 (Malitda) — Android

2026 장애인 분야 해커톤 「장애 플러스 기술」 분야1(디지털 포용) · 팀 VOICE MATE.
비정형 발화 학생을 위한 **로컬 STT 입력 보조 앱**: 원문 보존 → 후보 선택 → 확인·수정 → 승인 → 공유. 승인 전에는 아무것도 밖으로 나가지 않는다.

## 구성

| 항목 | 내용 |
|---|---|
| 언어·UI | Kotlin 2.2 · Jetpack Compose(Material3) · Navigation |
| 저장 | Room + **SQLCipher**(키는 Android Keystore가 감싼 무작위 32바이트), DataStore |
| STT(기기 내) | **whisper.cpp**(ggml-base-q5_1, JNI) · **Vosk**(vosk-model-small-ko-0.22) — 설정에서 전환, 한 번에 하나만 메모리에 올림 |
| TTS | 시스템 TextToSpeech(오프라인 한국어 음성 우선) |
| 네트워크 | **인터넷 권한 없음.** 외부 STT API·클라우드 전환 없음 |
| 화면 | 화면 계약 S01~S27(디자인 패키지 `screen-contract.json`)과 1:1 라우트 |

핵심 규칙(`domain/`):
- `TextNormalizer` — M1 완전일치 판정용 정규화 3규칙(NFC·공백·문장 끝 부호).
- `SentenceCleanup` — 기획서의 "문장 정리 규칙": 비발화 토큰 제거 + 조사·어미 1회 결합. 모든 엔진·조건에 동일 적용.
- `CandidateBuilder` — 원문 + 실제 N-best(최대 3) + M1 규칙1(완전일치 승인문장)·규칙2(실제 후보 내 재정렬). 유사도로 새 문장을 만들지 않는다.
- `Approval` — 승인 토큰 = 문장 해시. 한 글자라도 바뀌면 공유 잠김.
- `EvalMetrics` — CER·WER·완전일치·Top-3·무응답·처리시간(설정 → 평가 도구, CSV 저장).

## 빌드

```bash
# 1) 모델·소스 받기(git에 없음)
bash scripts/fetch-model.sh      # Vosk 한국어 모델 → app/src/main/assets/model-ko (253MB)
bash scripts/fetch-whisper.sh    # whisper.cpp 1.9.4 → third_party/, ggml-base-q5_1.bin → app/src/main/assets/whisper (60MB)

# 2) 빌드 (JDK 17, Android SDK 36, NDK 27.2, CMake 3.22 필요; 프로젝트 경로는 ASCII 여야 함)
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease -PsubmitAbis=arm   # 제출용(arm64-v8a + armeabi-v7a), keystore.properties 필요
```

Windows: `local.properties`에 `sdk.dir=C:/Android/Sdk` 형식(슬래시)으로 적는다.

## 평가 도구

디버그·릴리스 공통. 앱 전용 폴더 `files/testaudio/`에 16kHz mono PCM16 WAV와 `refs.txt`(`파일명<TAB>참조문`)를 넣고 **설정 → 전체 음원 평가 실행**. 결과는 화면과 `files/eval/eval-*.csv`.

```bash
adb push x.wav /data/local/tmp/ && adb shell "cat /data/local/tmp/x.wav | run-as kr.voicemate.malitda.debug sh -c 'cat > files/testaudio/x.wav'"
```

## 개인정보

원음성 파일 미저장, 인식 문장 로그 미기록, 공유 이력·상대방 미저장, 사용자별 분리 저장, 앱 안에서 개별 삭제·전체 초기화, 백업 제외(`data_extraction_rules`).
