# 말잇다 2026-10-28 개발자 인계

기준은 버전 **0.3.4**, 버전 코드 **13**, DB 스키마 **5**다. 마이크 배경 자산을 포함한 최종 C 빌드에서 단위 검사 118개와 핵심 계측 검사 67개를 통과했다. 지정한 C 실행·화면·미디어 검수를 마쳤으며 팀 서명 인계 준비 상태다. 산출물·원시 로그의 SHA-256과 검사 범위는 [verification.json](verification.json)에 기록한다.

검증한 앱 코드 커밋은 `f7adad2ec82881a20a2769311a64ae423030a857`이다. 커밋 전후 소스 21개 파일의 바이트가 보존됐으며 검증 APK 해시는 동일하다.

## 반영 범위

- C 표면 마감: 원안의 크기·배치·그림 비율·Jua Regular·기본 100%·하단 4탭을 유지한다. 기존 색면에 얇은 하이라이트와 부드러운 접촉 그림자를 적용한다. 기존 누름 효과와 OS 모션 끄기 대응을 유지한다.
- 생성 자산의 범위: 기본 홈 마이크 색면에 단일 생성 이미지 `@drawable/mic_surface_c`를 사용한다. 마이크 외곽 170dp와 문구 21sp를 유지하며 글자·아이콘은 네이티브로 그린다. 고대비 또는 글자 배율 1.4 이상에서는 자산을 끄고 기존 그리기로 돌아간다.
- 프로필별 듣기 시간: 기본 20초, 60초, 직접 끝내기(저장값 0)를 제공한다. 녹음 시작 전에 해당 프로필의 저장값을 읽고 이번 녹음에 고정한다. 직접 끝내기는 자동종료를 예약하지 않는다. 취소·프로필 전환·백그라운드 이동·세션 종료 때 녹음을 정리한다.
- 홈 개인 뜻 우선: 홈에 표시할 수 있는 개인 뜻을 연습 예시 앞에 배치한다. 각 그룹 안의 기존 순서와 자료·확인 상태는 유지한다.

기본 흐름은 음성 또는 직접 입력 → 원문·후보와 뜻 확인 → 승인 또는 거절·모름·중단 → TTS·복사·호환 앱 공유다. 개인 뜻 등록, 지원자 PIN, 미확인 보관, 선택적 수정 저장을 유지한다. 원음 파일의 저장·업로드나 자동 승인을 추가하지 않는다.

## 검증 상태

| 구분 | 현재 상태 | 근거와 적용 범위 |
|---|---|---|
| 최종 C 단위 보고서 | 118개 통과·실패 0·오류 0·22개 suite | 현재 XML 집계와 최종 APK 검증 기록 대조 |
| 최종 C 핵심 계측 | 67개 통과·실패 0 | 소유 AVD·합성 자료. 상태/UI 검사에는 가짜 엔진을 사용하며, 실제 Whisper와 합성 WAV 통합 1건도 이67개 안에 포함 |
| 최종 C debug APK | 빌드·서명 검증 통과 | 실측 3개 ABI |
| 최종 C release APK | ARM/R8 빌드·lintRelease 통과 | 실측 2개 ARM ABI, 팀 키 부재로 unsigned |
| 패키지 무결성 | 두 APK 모두 통과 | 모델 15개 SHA 일치·ZIP CRC 통과·공급자 키 형태 탐지 0 |
| 최초 홈 실행 화면 | 확인 통과 | 홈의 핵심 11개 노드 글자·bounds가 baseline과 같음 |
| 실제 화면·설정 | 지정 범위 통과 | 390dp 11화면, 430dp 홈·뜻·등록, 320dp 200% 스크롤·자산 OFF, 앱·시스템 고대비, OS 모션 0 |
| IME·권한·TTS 경로 | 통과 | 실제 키보드 위 등록 CTA, 권한 거절→안내→설정 허용→듣기 취소, TTS 표시·중지·복귀. 음질은 미검수 |
| PNG·영상 | 통과 | 실제 앱 PNG 11개, 90.000초·1920×1080·H.264·30fps·2700프레임·무음 영상 |
| 실행 정리 | 통과 | 20초·앱/시스템 100%·390dp·권한 해제·고대비 원래 값·모션 1 복원. 기존 자료 보존 후 소유 AVD 종료 |
| 숫자 경고 격리 검사 | 별도 1건 통과 | 동일 앱 APK, 별도 계측 APK로 실행. 기존 67개 실행과 분리 |
| 모델 원본 무결성 | 15/15 일치 | 로컬 모델을 해시 정본과 직접 대조. 파일별 결과는 verification.json |
| 이전 baseline 계측 검사 | 과거 67개 통과 | C 표면 마감 전 기록이며 현재 후보 결과와 분리 |

계측 로그의 JUnit 표시 시간은 **131.054초**다. 실행 receipt의 설치 등 호스트 전체 구간은 **142.304854초**다. 두 시간은 측정 범위가 다르며 성능 개선 비교에 사용하지 않는다. 계측 과정에서 기존 앱 삭제·자료 초기화를 수행하지 않았다. 별도 숫자 경고 검사는 러너 12.854초·호스트 14.783907초이며 67개 핵심 실행에 합산하지 않는다.

debug lint는 오류 0·경고 40·힌트 14, release lint는 오류 0·경고 41·힌트 14다. 차이 1건은 기존 적응형 런처 아이콘의 `MonochromeLauncherIcon` 경고이며 이번 C 표면 마감 범위에서 수정하지 않았다.

기존67개 중 `SyntheticSpeechIntegrationTest`는 로컬 Heami 합성 “물 마시고 싶어요”를 실제 번들 Whisper 모델·JNI로 전사하고, 미리 고정한 개인 뜻 후보에 연결되는지 검사했다. 빈 힌트를 사용했으며 원문 비어 있지 않음과 기대 후보 포함을 확인한다. 보존 로그의 실제 전사도 같은 문장이지만 전체 문장 일치가 assertion인 것은 아니다. 이번 재감사에서 새 시험·녹음·빌드는 실행하지 않았다. 실제 사람/장애 발화 정확도·마이크부터 전달까지의 전체 경로·Vosk 실제 전사·현장 효과를 입증하는 근거로 확대하지 않는다. [합성 음성 검증 범위](synthetic-speech-evidence.md).

이전 baseline의 67개 통과와 자산 추가 전 C v2의 최초 보고는 `historical_reference`에 별도로 보관한다. 실제 이용자의 발화·의사소통 효과, 실제 외부 앱 최종 발송, 실물 TTS 음질·재생, 실제 ARM 휴대전화 실행, 운영 R4와 유료 AI 생성·과금·서버 취소 효과는 별도 미실행 범위다. TalkBack·스위치·외부 키보드의 전체 포커스와 앱 전체 AA 대비 인증, 태블릿·전체 경로·저장 고갈 주입도 이번 지정 범위 밖이다.

PNG 11개의 SHA-256·청크 CRC와 영상 실물 SHA-256을 직접 대조했다. 영상 전체 디코드 오류는 0이며 6장면의 시작·중간·끝 18개 표본을 검수했다. 마지막 구간의 60초 선택과 20초 선택도 확인했다. 이전 PNG 11개와 영상 1개, 총 12개 보관본의 SHA는 보존됐다.

영상 SHA-256: `f90dbb8bd24e82d25dca1753a521f5f37ddf8378a518a56740cdafebc32b4114`. 실제 앱 미디어는 합성 예시만 사용했으며 시연 중 유료 API를 호출하지 않았다.

## 확정 산출물

| 산출물 | 바이트 | APK에서 확인한 ABI | 서명 |
|---|---:|---|---|
| debug | 276,787,599 | arm64-v8a·armeabi-v7a·x86_64 | debug 서명 검증 exit 0 |
| release | 184,163,473 | arm64-v8a·armeabi-v7a | unsigned, 검증 exit 1 |

- debug SHA-256: `dae8d8ff5626a6312c7df8f94ce2eff1d97629562622f6dc8745961ca117ee56`
- release unsigned SHA-256: `6aea05c6996ecdd595947270c927cea6d1329e796cf5a8a9b474b6fea646156c`

이번 debug 실물은 `app/build/intermediates/apk/debug/app-debug.apk`, 계측 APK는 `app/build/intermediates/apk/androidTest/debug/app-debug-androidTest.apk`에 있다. debug의 APK 목록 연결 파일은 `app/build/intermediates/apk_ide_redirect_file/debug/createDebugApkListingFileRedirect/redirect.txt`이며 실제 `output-metadata.json`을 가리킨다. 일반적으로 안내되는 `app/build/outputs/apk/debug/app-debug.apk`는 이번 환경에 실물이 없으므로 존재와 해시를 확인한 뒤 사용한다.

release 실물은 `app/build/outputs/apk/release/app-release-unsigned.apk`다. unsigned 검증 exit 1은 현재의 미서명 상태를 나타낸다. 팀 키로 서명하기 전에는 설치용 릴리스로 확정할 수 없다.

## 모델과 빌드 준비

모델 바이너리와 Whisper C++ 소스는 저장소에 포함하지 않는다. 기존 [모델 준비 스크립트](../../scripts/prepare-models.ps1)와 [SHA-256 정본](../../scripts/model-sha256.properties)을 사용한다. 정본은 Vosk 한국어 모델 14개 파일과 Whisper 모델 1개 파일로 구성된다. 라이선스는 [모델 라이선스 폴더](../../app/src/main/assets/licenses)에 있다. 해시가 다른 기존 파일은 덮어쓰기 전에 확인한다.

저장소 설정은 JDK 17, Android SDK 36, NDK 27.2.12479018, CMake 3.22.1, Gradle Wrapper 8.14.3을 사용한다. 최소 Android 버전은 8.0(API 26)이다. 설치와 원본 준비는 [기존 README](../../README.md)를 따른다.

저장소 루트에서 실행한다. Whisper 소스가 기본 `third_party/whisper.cpp`에 있으면 `-PwhisperCppDir` 인수를 생략할 수 있다.

```powershell
.\gradlew.bat :app:verifyBundledSpeechModels --console=plain
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug -PincludeDirectImageKey=false -PenablePrivatePilotTools=false --console=plain
.\gradlew.bat :app:assembleDebug -PwhisperCppDir=C:/path/to/whisper.cpp -PincludeDirectImageKey=false -PenablePrivatePilotTools=false --console=plain
.\gradlew.bat :app:assembleRelease :app:lintRelease -PsubmitAbis=arm -PwhisperCppDir=C:/path/to/whisper.cpp -PincludeDirectImageKey=false -PenablePrivatePilotTools=false --console=plain
```

`-PsubmitAbis=arm`은 ARM 빌드를 요청하는 설정이다. 실제 ABI는 최종 APK의 `lib/` 항목으로 확정한다. 이번 debug는 3개 ABI, release는 2개 ARM ABI를 실측했다. 패키징의 `mergeDebugAssets`·`mergeReleaseAssets`는 `verifyBundledSpeechModels`에 의존하므로 모델 15개가 모두 존재하고 해시가 일치해야 한다.

## 서명과 기존 앱 자료

| 빌드 | 패키지 | 용도 |
|---|---|---|
| debug | `kr.voicemate.malitda.debug` | 기존 배포 앱과 분리된 검증용 앱 |
| release | `kr.voicemate.malitda` | 팀의 기존 릴리스 서명을 사용하는 배포 후보 |

공개 검증·서명 인계 빌드는 `-PincludeDirectImageKey=false -PenablePrivatePilotTools=false`를 명시한다. 릴리스 업데이트에는 팀의 **기존 서명 키**가 필요하다. `keystore.properties`와 키 파일은 팀이 별도 보관한다. 현재 release는 unsigned이며 팀 키로 서명한 설치용 APK가 필요하다.

서명이 맞지 않으면 기존 앱을 삭제하지 않는다. 기존 사전·PIN·설정을 보존하고 같은 패키지·기존 서명으로 다시 빌드한다. 별도 debug 앱은 기존 release 자료를 자동으로 공유하지 않는다. 팀 서명과 실제 ARM 휴대전화 실행은 남아 있다.

## AI와 공개 문서의 경계

release는 R4 주소와 공급자 키가 빈값이며 외부 AI 그림 기능이 꺼져 있다. 최종 release APK에는 INTERNET 권한도 없다. 기본 debug에도 공급자 키를 넣지 않는다. debug의 모의 R4 개발 경로는 이번 C 시각 검수에서 실행하지 않았다(`not_exercised`). 운영 R4와 실제 유료 생성·비용·취소 효과는 별도 범위다. 일반 debug·release의 실증 도구는 기본 꺼짐이다.

이 폴더에는 공개 가능한 코드·빌드·검증 상태와 산출물·원시 로그의 SHA-256만 기록한다. 개인별·현장 실증 자료, 집계 문서, 사람 사진, 서명, 참석 명단, 로컬 절대 경로와 비밀키는 포함하지 않는다.
