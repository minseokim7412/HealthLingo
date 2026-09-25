#!/usr/bin/env bash
# 헬스링고 빌드 스크립트 (Maven/Gradle 없이 javac만 사용)
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name "*.java" > /tmp/hlg_sources.txt
javac -d out -encoding UTF-8 @/tmp/hlg_sources.txt
echo "빌드 완료: out/ 디렉터리에 클래스 파일이 생성되었습니다."
echo "실행: java -Dstdout.encoding=UTF-8 -cp out com.healthlingo.Main"
