# HealthLingo (헬스링고)

개인 맞춤형 운동 관리 CLI 애플리케이션 

## 개요

- **플랫폼**: Java 단일 CLI 실행 프로그램 (외부 라이브러리 없음)
- **저장 방식**: JSON 파일 기반 로컬 저장 (`data/` 폴더, 9개 파일)
- **아키텍처**: Console UI → 5개 Manager(운동/신체/알림/보상/통계) → Validator → JsonStorage → JSON 파일

## 빌드 및 실행

Maven/Gradle 없이 `javac`만으로 빌드합니다.

```bash
./build.sh
./run.sh
```

또는 직접 실행:

```bash
find src -name "*.java" > sources.txt
javac -d out -encoding UTF-8 @sources.txt
java -Dstdout.encoding=UTF-8 -cp out com.healthlingo.Main
```

Windows(PowerShell)에서 한글이 깨질 경우 콘솔 코드페이지를 UTF-8로 전환하세요.

```powershell
chcp 65001
java -Dstdout.encoding=UTF-8 -cp out com.healthlingo.Main
```

## 주요 기능 (설계서 FR-01~05)

1. **운동 기록/루틴** - 종목 자유 입력·자동 등록, 날짜별 기록, 루틴 관리
2. **신체 기록** - 체중·체지방률·골격근량 기록 및 BMI 자동 계산
3. **알림** - 목표 미달·미출석 감지, 하루 1회 경고
4. **보상** - 누적 조건 달성 포인트 지급, 확률 기반 뱃지 뽑기
5. **통계** - 기간별(일/주/월/전체) 통계·텍스트 그래프·달성률

예외 처리(EH-01~05: 입력 형식 오류, 존재하지 않는 날짜 조회, 신체 정보 미입력 시 통계/BMI 계산 생략, 포인트 부족)도 설계서와 동일하게 구현했습니다.

## 소스 구조

```
src/com/healthlingo/
  Main.java              # 진입점
  json/                  # 경량 JSON 파서/직렬화 (org.json 대체, 외부 의존성 없음)
  storage/JsonStorage.java
  model/                 # 9개 데이터 엔터티
  validator/             # InputValidator(EH-01), BodyProfileValidator(EH-03/05)
  manager/               # ExerciseCatalogManager, WorkoutManager, BodyManager,
                         # NotificationManager, RewardManager, StatisticsManager
  ui/ConsoleUI.java      # 화면 UI-001~006
```

> 설계서는 파일 입출력에 `org.json` 라이브러리 사용을 전제했으나, 본 실행 환경에 Maven/Gradle 및
> 인터넷 접근이 없어 동일한 역할(JsonStorage 공통 파일 입출력)을 수행하는 경량 JSON 파서를 자체
> 구현했습니다. 모듈 책임과 인터페이스는 설계서와 동일합니다.
