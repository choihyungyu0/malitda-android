# 실제 Whisper 합성 음성 검사 범위

2026-10-28 보존 증빙 재감사. 제품·시험·APK를 바꾸거나 재실행한 결과가 아니다.

최종 C 핵심67개 가운데1개가 실제 번들 Whisper 모델로 한국어 합성 음성1문장을 전사하고 개인 뜻 후보와 연결했다. 별도 숫자 경고 화면1건과 다른 검사이며 전체 개수에 추가하지 않는다.

- 입력: 로컬 Windows Heami 합성 “물 마시고 싶어요”,16kHz·mono·PCM16. 사람 녹음과 클라우드 사용은 없다.
- WAV SHA-256: `325ee417e926da55f122333b4e549b9521753c4ab8a3c7cc5c99871a2e5954f7`.
- 경로: `WhisperSttEngine.prepare/recognizeWav` → `WhisperNative.transcribe` → 실제 `MeaningResolver`. 모델은 번들 `ggml-base-q5_1.bin`이며 인식 힌트는 빈 목록이다.
- assertion: 엔진 준비·비어 있지 않은 전사·독립적으로 고정한 기대 개인 뜻 후보 포함. 정확한 전체 문장 일치는 assertion이 아니다.
- 보존 로그 관찰: 원문 “물 마시고 싶어요”, 기대 뜻 후보 포함. 전사 처리시간은 이1건의 합성 가상기기 관찰값이며 성능 개선·실제 이용자 지연 지표로 제시하지 않는다.

앱 SHA는 `dae8d8ff5626a6312c7df8f94ce2eff1d97629562622f6dc8745961ca117ee56`, core시험APK SHA는 `8bd95a9c057ce9710b7eb579bd0aebbfd6bcca2f73412cdd95e43514c455b17a`다. 소프트웨어 코드commit은 `f7adad2ec82881a20a2769311a64ae423030a857`로 유지된다.

source: `app/src/androidTest/java/kr/voicemate/malitda/stt/SyntheticSpeechIntegrationTest.kt` 및 `app/src/androidTest/assets/synthetic-water.provenance.json`. 로컬 final C `full-instrumentation.log`753-767줄의54/67 PASS와 `instrumentation-logcat.log`1315줄의 결과를 대조했다. 관련 전체 파일 SHA는 기존 verification.json의 evidence를 사용한다.

Vosk PCM 실제 전사, 마이크 입력부터 최종 전달까지의 전체E2E, 사람·장애 발화 정확도, 물리 ARM 성능, 실제 음질과 현장 효과는 별도 미검증이다. ‘67개 전부 fake STT’와 ‘실제 이용자 음성 성능 입증’ 어느 쪽으로도 표현하지 않는다.
