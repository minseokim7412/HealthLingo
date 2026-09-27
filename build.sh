#!/usr/bin/env bash
# 헬스링고 빌드 스크립트 (Maven/Gradle 없이 javac만 사용)
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name "*.java" > /tmp/hlg_sources.txt
javac -d out -encoding UTF-8 @/tmp/hlg_sources.txt

# 설계서 9장(운영성): 단일 실행 .jar 파일로 배포 가능하도록 패키징
# 일부 환경은 PATH에 java/javac만 있고 jar가 빠져 있어(java.home 기준으로) 직접 찾는다.
JAR_CMD="jar"
if ! command -v jar > /dev/null 2>&1; then
  JAVA_HOME_DIR=$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java\.home/{print $2}' | tr -d '\r')
  JAR_CMD="$(cygpath -u "$JAVA_HOME_DIR" 2>/dev/null || echo "$JAVA_HOME_DIR")/bin/jar"
fi
"$JAR_CMD" cfe healthlingo.jar com.Main -C out .

echo "빌드 완료: healthlingo.jar 생성됨 (클래스 파일은 out/ 에도 있음)"
echo "실행: java -Dstdout.encoding=UTF-8 -jar healthlingo.jar"
