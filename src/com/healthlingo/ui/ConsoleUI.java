package com.healthlingo.ui;

import com.healthlingo.manager.BodyManager;
import com.healthlingo.manager.ExerciseCatalogManager;
import com.healthlingo.manager.NotificationManager;
import com.healthlingo.manager.RewardManager;
import com.healthlingo.manager.StatisticsManager;
import com.healthlingo.manager.WorkoutManager;
import com.healthlingo.model.BadgeRecord;
import com.healthlingo.model.BodyRecord;
import com.healthlingo.model.Routine;
import com.healthlingo.model.SetRecord;
import com.healthlingo.model.UserProfile;
import com.healthlingo.model.WorkoutRecord;
import com.healthlingo.validator.InputValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console UI Layer
 * 메뉴 출력과 사용자 입력·출력 처리를 담당하며, 각 Manager를 호출한다.
 * 화면 ID UI-001~UI-006에 대응한다.
 */
public class ConsoleUI {

    private static final String LINE = "===================================";
    private static final String SUB_LINE = "-----------------------------------";

    private final Scanner sc = new Scanner(System.in);
    private final InputValidator val = new InputValidator();

    private final WorkoutManager wm;
    private final BodyManager bm;
    private final NotificationManager nm;
    private final RewardManager rm;
    private final StatisticsManager sm;
    private final ExerciseCatalogManager cat;

    public ConsoleUI(WorkoutManager wm, BodyManager bm, NotificationManager nm,
                      RewardManager rm, StatisticsManager sm, ExerciseCatalogManager cat) {
        this.wm = wm;
        this.bm = bm;
        this.nm = nm;
        this.rm = rm;
        this.sm = sm;
        this.cat = cat;
    }

    public void run() {
        ensureProfile();

        // FR-03: 프로그램 시작 직후 출석/알림 자동 체크 (오늘 출석 기록 전에 계산해야 미접속 일수가 정확하다)
        List<String> msgs = nm.checkNotification();
        nm.recordAttendanceToday();
        rm.checkMilestones("ATTEND", nm.getAttendanceCount());

        boolean go = true;
        while (go) {
            go = showMainMenu(msgs);
            msgs = new ArrayList<>(); // 알림은 메인 메뉴 최초 진입 시 1회만 표시
        }
        System.out.println("[완료] 데이터가 저장되었습니다. 헬스링고를 종료합니다.");
    }

    private void ensureProfile() {
        UserProfile prof = bm.getProfile();
        if (prof != null) return;

        System.out.println(LINE);
        System.out.println(" 헬스링고 (Health-Lingo) 최초 실행");
        System.out.println(LINE);
        String name = val.readNonEmpty(sc, "이름을 입력하세요 >> ");
        System.out.println("신장(m) 입력 >> 나중에 입력하려면 0을 입력하세요.");
        double height = val.readNonNegativeDouble(sc, "신장(m) >> ");
        bm.registerProfile(name, height, LocalDate.now().toString());
        System.out.println("[완료] 프로필이 등록되었습니다.\n");
    }

    /** UI-001 메인 메뉴 (경고/알림 화면 UI-006 포함) */
    private boolean showMainMenu(List<String> msgs) {
        System.out.println();
        System.out.println(LINE);
        System.out.println(" 헬스링고 (Health-Lingo)");
        System.out.println(LINE);
        for (String msg : msgs) System.out.println(msg);
        System.out.println(SUB_LINE);
        System.out.println("1. 운동 기록 및 루틴");
        System.out.println("2. 신체 변화 기록");
        System.out.println("3. 뱃지 · 보상");
        System.out.println("4. 통계 보기");
        System.out.println("5. 종료");
        int ch = val.readMenuChoice(sc, "번호를 입력하세요 >> ", 1, 5);

        switch (ch) {
            case 1: workoutMenu(); break;
            case 2: bodyMenu(); break;
            case 3: rewardMenu(); break;
            case 4: statsMenu(); break;
            case 5: return false;
        }
        return true;
    }

    /** UI-002 운동 기록 메뉴 (FR-01) */
    private void workoutMenu() {
        boolean go = true;
        while (go) {
            System.out.println();
            System.out.println(LINE);
            System.out.println(" 운동 기록 및 루틴");
            System.out.println(LINE);
            System.out.println("1. 오늘 운동 등록하기");
            System.out.println("2. 날짜별 운동 기록 조회");
            System.out.println("3. 루틴 확인하기 / 추가하기");
            System.out.println("4. 목표 설정하기");
            System.out.println("0. 이전 메뉴로");
            int ch = val.readMenuChoice(sc, "번호를 입력하세요 >> ", 0, 4);
            switch (ch) {
                case 1: addWorkoutFlow(); break;
                case 2: viewWorkoutByDateFlow(); break;
                case 3: routineFlow(); break;
                case 4: setGoalFlow(); break;
                case 0: go = false; break;
            }
        }
    }

    private void addWorkoutFlow() {
        String today = LocalDate.now().toString();
        String exerciseName = val.readNonEmpty(sc, "종목명을 입력하세요 >> ");
        int nSet = val.readPositiveInt(sc, "세트 수를 입력하세요 >> ");
        List<SetRecord> sets = new ArrayList<>();
        for (int i = 1; i <= nSet; i++) {
            double weight = val.readNonNegativeDouble(sc, i + "세트 무게(kg) >> ");
            int reps = val.readPositiveInt(sc, i + "세트 횟수 >> ");
            sets.add(new SetRecord(weight, reps));
        }
        wm.addWorkout(today, exerciseName, sets);
        rm.checkMilestones("WORKOUT", wm.getWorkoutCount());
        System.out.println("[완료] " + today + " 운동 기록이 저장되었습니다.");
    }

    private void viewWorkoutByDateFlow() {
        String date = val.readDate(sc, "조회할 날짜를 입력하세요 (yyyy-MM-dd) >> ");
        List<WorkoutRecord> list = wm.getWorkoutsByDate(date);
        if (list.isEmpty()) {
            // EH-02: WorkoutManager가 안내 메시지를 이미 출력했으므로 메뉴로 복귀만 한다.
            return;
        }
        System.out.println("[" + date + " 운동 기록]");
        for (WorkoutRecord r : list) {
            StringBuilder sb = new StringBuilder("- 종목: " + cat.getNameById(r.getExerciseId()) + " | 세트: ");
            for (SetRecord s : r.getSets()) sb.append(s.getWeight()).append("kg x ").append(s.getReps()).append("회  ");
            System.out.println(sb);
        }
    }

    private void routineFlow() {
        List<Routine> rts = wm.getRoutines();
        if (rts.isEmpty()) {
            System.out.println("[알림] 등록된 루틴이 없습니다.");
        } else {
            System.out.println("[등록된 루틴]");
            for (Routine r : rts) {
                System.out.println("- " + r.getName() + " (" + r.getCycle() + ") 종목 수: " + r.getExerciseIds().size());
            }
        }
        int ch = val.readMenuChoice(sc, "루틴을 추가하시겠습니까? (1: 예, 0: 아니오) >> ", 0, 1);
        if (ch == 1) {
            String name = val.readNonEmpty(sc, "루틴 이름 >> ");
            String cycle = val.readNonEmpty(sc, "적용 요일/주기 (예: 월,수,금) >> ");
            int count = val.readPositiveInt(sc, "루틴에 포함할 종목 수 >> ");
            List<String> names = new ArrayList<>();
            for (int i = 1; i <= count; i++) names.add(val.readNonEmpty(sc, i + "번째 종목명 >> "));
            wm.addRoutine(name, cycle, names);
            System.out.println("[완료] 루틴이 등록되었습니다.");
        }
    }

    private void setGoalFlow() {
        String exerciseName = val.readNonEmpty(sc, "목표를 설정할 종목명 >> ");
        double tgt = val.readPositiveDouble(sc, "목표 무게(kg) >> ");
        String period = val.readNonEmpty(sc, "적용 기간(예: 2026-09) >> ");
        nm.setGoal(exerciseName, tgt, period);
        System.out.println("[완료] 목표가 설정되었습니다.");
    }

    /** UI-003 신체 관리 메뉴 (FR-02, EH-05) */
    private void bodyMenu() {
        boolean go = true;
        while (go) {
            System.out.println();
            System.out.println(LINE);
            System.out.println(" 신체 변화 기록");
            System.out.println(LINE);
            System.out.println("1. 신체 정보 등록/수정");
            System.out.println("2. BMI 및 이력 조회");
            System.out.println("0. 이전 메뉴로");
            int ch = val.readMenuChoice(sc, "번호를 입력하세요 >> ", 0, 2);
            switch (ch) {
                case 1: registerBodyFlow(); break;
                case 2: viewBodyHistoryFlow(); break;
                case 0: go = false; break;
            }
        }
    }

    private void registerBodyFlow() {
        String id = "";
        List<BodyRecord> existing = bm.getAllRecords();
        if (!existing.isEmpty()) {
            System.out.println("[기존 신체 기록]");
            for (BodyRecord r : existing) {
                System.out.println("- " + r.getId() + " | " + r.getDate() + " | " + r.getWeight() + "kg");
            }
            id = val.readOptional(sc, "수정할 기록 ID (신규 등록은 그냥 Enter) >> ");
            if (!id.isEmpty() && bm.findById(id) == null) {
                System.out.println("[알림] 해당 ID의 기록이 없어 신규 등록으로 진행합니다.");
                id = "";
            }
        }

        String date = id.isEmpty() ? LocalDate.now().toString() : bm.findById(id).getDate();
        double weight = val.readPositiveDouble(sc, "체중(kg) 입력 >> ");
        double bodyFat = val.readNonNegativeDouble(sc, "체지방률(%) 입력 >> ");
        double muscleMass = val.readNonNegativeDouble(sc, "골격근량(kg) 입력 >> ");
        BodyRecord rec = bm.upsertBodyRecord(id, date, weight, bodyFat, muscleMass);

        if (bm.isBmiCalculated(rec)) {
            System.out.println("[완료] BMI " + String.format("%.1f", rec.getBmi()) + "로 계산되어 저장되었습니다.");
        } else {
            // EH-05: 신체 정보(신장) 미입력 상태에서는 계산을 생략하고 안내한다.
            System.out.println("[알림] 신장 정보가 없어 BMI를 계산할 수 없습니다.");
            System.out.println("       신체 정보를 먼저 등록해 주세요. (EH-05)");
        }
    }

    private void viewBodyHistoryFlow() {
        List<BodyRecord> recs = bm.getAllRecords();
        if (recs.isEmpty()) {
            System.out.println("[알림] 등록된 신체 기록이 없습니다.");
            return;
        }
        for (BodyRecord r : recs) {
            String bTxt = bm.isBmiCalculated(r) ? String.format("%.1f", r.getBmi()) : "계산 생략(EH-05)";
            System.out.println("- " + r.getDate() + " | 체중 " + r.getWeight() + "kg | 체지방률 " + r.getBodyFat()
                    + "% | 골격근량 " + r.getMuscleMass() + "kg | BMI " + bTxt);
        }
    }

    /** UI-004 뱃지·보상 메뉴 (FR-04, EH-04) */
    private void rewardMenu() {
        boolean go = true;
        while (go) {
            System.out.println();
            System.out.println(LINE);
            System.out.println(" 뱃지 · 보상");
            System.out.println(LINE);
            System.out.println("보유 포인트: " + rm.getCurrentPoint() + " P");
            System.out.println("1. 뱃지 뽑기 (100P 소모)");
            System.out.println("2. 보유 뱃지 목록 보기");
            System.out.println("0. 이전 메뉴로");
            int ch = val.readMenuChoice(sc, "번호를 입력하세요 >> ", 0, 2);
            switch (ch) {
                case 1: drawBadgeFlow(); break;
                case 2: viewBadgesFlow(); break;
                case 0: go = false; break;
            }
        }
    }

    private void drawBadgeFlow() {
        RewardManager.DrawResult res = rm.drawBadge();
        if (!res.ok) {
            // EH-04: 포인트 부족 시 뽑기 차단 및 경고 후 메뉴 복귀
            System.out.println("[알림] 포인트가 부족합니다. (보유 " + rm.getCurrentPoint() + "P / 필요 100P) (EH-04)");
            return;
        }
        if (res.miss) {
            System.out.println("[알림] 아쉽지만 꽝입니다! 다음 기회에 도전해 보세요.");
        } else {
            System.out.println("[완료] '" + res.bName + "' 뱃지(" + res.bGrd + " 등급)를 획득했습니다!");
        }
        System.out.println("남은 포인트: " + rm.getCurrentPoint() + " P");
    }

    private void viewBadgesFlow() {
        List<BadgeRecord> owned = rm.getOwnedBadges();
        if (owned.isEmpty()) {
            System.out.println("[알림] 보유한 뱃지가 없습니다.");
            return;
        }
        for (BadgeRecord r : owned) {
            System.out.println("- " + rm.getBadgeName(r.getBadgeId()) + " ("
                    + rm.getBadgeGrade(r.getBadgeId()) + ") x" + r.getCount()
                    + " | 최초 획득일: " + r.getFirstObtainedDate());
        }
    }

    /** UI-005 통계 메뉴 (FR-05, EH-03) */
    private void statsMenu() {
        System.out.println();
        System.out.println(LINE);
        System.out.println(" 통계 보기");
        System.out.println(LINE);
        System.out.println("조회 기간을 선택하세요");
        System.out.println("1. 일간   2. 주간   3. 월간   4. 전체");
        System.out.println("0. 이전 메뉴로");
        int ch = val.readMenuChoice(sc, "번호를 입력하세요 >> ", 0, 4);
        if (ch == 0) return;

        StatisticsManager.Period period;
        switch (ch) {
            case 1: period = StatisticsManager.Period.DAILY; break;
            case 2: period = StatisticsManager.Period.WEEKLY; break;
            case 3: period = StatisticsManager.Period.MONTHLY; break;
            default: period = StatisticsManager.Period.ALL; break;
        }

        StatisticsManager.StatResult res = sm.getStatistics(period);
        System.out.println();
        System.out.println("[" + res.label + " 통계]");
        System.out.println("총 운동 횟수 : " + res.total + "회");
        for (String line : res.lines) System.out.println(line);
        if (res.bOk) {
            System.out.println("체중 변화     : " + res.wLine);
        } else {
            // EH-03: 신체 정보 미입력 시 관련 판정을 생략하고 안내한다.
            System.out.println("[알림] 신체 정보가 없어 체중·BMI 통계는 생략합니다. (EH-03)");
        }
    }
}
