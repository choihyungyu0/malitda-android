#!/usr/bin/env bash
# whisper.cpp 소스(third_party/whisper.cpp)와 ggml-base-q5_1 모델(app/src/main/assets/whisper)을 받는다.
# 두 항목 모두 git에 넣지 않는다(용량). 빌드 전에 한 번 실행.
set -e
cd "$(dirname "$0")/.."
VER=1.9.4
if [ ! -f third_party/whisper.cpp/include/whisper.h ]; then
  mkdir -p third_party tmp-whisper
  curl -L -o tmp-whisper/whisper.tar.gz "https://github.com/ggml-org/whisper.cpp/archive/refs/tags/v$VER.tar.gz"
  tar -xzf tmp-whisper/whisper.tar.gz -C tmp-whisper
  rm -rf third_party/whisper.cpp
  mv "tmp-whisper/whisper.cpp-$VER" third_party/whisper.cpp
  rm -rf third_party/whisper.cpp/{examples,tests,samples,media,bindings,models,ci,grammars,scripts} tmp-whisper
  echo "whisper.cpp $VER -> third_party/whisper.cpp"
else
  echo "whisper.cpp already present"
fi
DST=app/src/main/assets/whisper/ggml-base-q5_1.bin
if [ ! -f "$DST" ]; then
  mkdir -p app/src/main/assets/whisper
  curl -L -o "$DST" "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base-q5_1.bin"
  echo "model -> $DST"
else
  echo "model already present"
fi
