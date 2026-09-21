#!/usr/bin/env bash
# 제출용 서명 APK 빌드: arm64-v8a + armeabi-v7a, R8 축소, 파일명 규칙 "팀 이름_개발물 이름_분야.apk"
set -e
cd "$(dirname "$0")/.."
[ -f keystore.properties ] || { echo "keystore.properties 가 없어요(서명 정보)"; exit 1; }
[ -f app/src/main/assets/model-ko/am/final.mdl ] || bash scripts/fetch-model.sh
[ -f app/src/main/assets/whisper/ggml-base-q5_1.bin ] || bash scripts/fetch-whisper.sh
./gradlew :app:assembleRelease -PsubmitAbis=arm --console=plain
mkdir -p dist
OUT="dist/VOICE MATE_말잇다_분야1.apk"
cp app/build/outputs/apk/release/app-release.apk "$OUT"
ls -la dist/
echo "SHA-256: $(sha256sum "$OUT" | cut -d' ' -f1)"
