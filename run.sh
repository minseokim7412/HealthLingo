#!/usr/bin/env bash
# 헬스링고 실행 스크립트
set -e
cd "$(dirname "$0")"
if [ ! -d out ]; then
  ./build.sh
fi
java -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp out com.healthlingo.Main
