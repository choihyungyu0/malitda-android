#!/usr/bin/env bash
# Vosk 한국어 소형 모델을 받아 app/src/main/assets/model-ko 에 풀어 놓는다(APK 동봉용).
set -e
cd "$(dirname "$0")/.."
DST=app/src/main/assets/model-ko
if [ -f "$DST/am/final.mdl" ]; then echo "model already present: $DST"; exit 0; fi
mkdir -p app/src/main/assets tmp-model
curl -L -o tmp-model/model.zip https://alphacephei.com/vosk/models/vosk-model-small-ko-0.22.zip
unzip -q -o tmp-model/model.zip -d tmp-model
rm -rf "$DST"; mv tmp-model/vosk-model-small-ko-0.22 "$DST"; rm -f "$DST/README"; rm -rf tmp-model
echo "done: $DST"
