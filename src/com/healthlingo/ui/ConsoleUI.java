package com.healthlingo.ui;

import com.healthlingo.manager.BodyManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
    public ConsoleUI(WorkoutManager wm, BodyManager bm, NotificationManager nm,
                      RewardManager rm, StatisticsManager sm) {
        this.wm = wm;
        this.bm = bm;
        this.nm = nm;
        this.rm = rm;
        this.sm = sm;
    }
    public void run() {
        ensureProfile();
        List<String> msgs = nm.checkNotification();
        nm.recordAttendanceToday();
        rm.checkMilestones("ATTEND", nm.getAttendanceCount());
        boolean go = true;
        while (go) {
            go = showMainMenu(msgs);
            msgs = new ArrayList<>();
        }
        Ansi.done("[완료] 데이터가 저장되었습니다. 헬스링고를 종료합니다.");
    }
    private void ensureProfile() {
        UserProfile prof = bm.getProfile();
        if (prof != null) return;
        System.out.println(LINE);
        System.out.println(" 헬스링고 (Health-Lingo) 최초 실행");
        System.out.println(LINE);
        bm.registerProfileInteractive(sc);
        Ansi.done("[완료] 프로필이 등록되었습니다.\n");
    }

    private boolean showMainMenu(List<String> msgs) {
        System.out.println();
        System.out.println(LINE);
        System.out.println(" 헬스링고 (Health-Lingo)");
        System.out.println(LINE);
        for (String msg : msgs) {
            if (msg.startsWith("[알림]")) Ansi.alert(msg);
            else System.out.println(msg);
        }
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
        WorkoutRecord rec = wm.addWorkoutInteractive(sc);
        rm.checkMilestones("WORKOUT", wm.getWorkoutCount());
        Ansi.done("[완료] " + rec.getDate() + " 운동 기록이 저장되었습니다.");
    }
    private void viewWorkoutByDateFlow() {
        List<WorkoutRecord> list = wm.getWorkoutsByDateInteractive(sc);
        if (list.isEmpty()) {
            return;
        }
        System.out.println("[" + list.get(0).getDate() + " 운동 기록]");
        for (WorkoutRecord r : list) {
            StringBuilder sb = new StringBuilder("- 종목: " + wm.getExerciseName(r.getExerciseId()) + " | 세트: ");
            for (SetRecord s : r.getSets()) sb.append(s.getWeight()).append("kg x ").append(s.getReps()).append("회  ");
            System.out.println(sb);
        }
    }
    private void routineFlow() {
        List<Routine> rts = wm.getRoutines();
        if (rts.isEmpty()) {
            Ansi.alert("[알림] 등록된 루틴이 없습니다.");
        } else {
            System.out.println("[등록된 루틴]");
            for (Routine r : rts) {
                System.out.println("- " + r.getName() + " (" + r.getCycle() + ") 종목 수: " + r.getExerciseIds().size());
            }
        }
        int ch = val.readMenuChoice(sc, "루틴을 추가하시겠습니까? (1: 예, 0: 아니오) >> ", 0, 1);
        if (ch == 1) {
            wm.addRoutineInteractive(sc);
            Ansi.done("[완료] 루틴이 등록되었습니다.");
        }
    }
    private void setGoalFlow() {
        String exerciseName = val.readNonEmpty(sc, "목표를 설정할 종목명 >> ");
        double tgt = val.readPositiveDouble(sc, "목표 무게(kg) >> ");
        String period = val.readNonEmpty(sc, "적용 기간(예: 2026-09) >> ");
        nm.setGoal(exerciseName, tgt, period);
        Ansi.done("[완료] 목표가 설정되었습니다.");
    }
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
                Ansi.alert("[알림] 해당 ID의 기록이 없어 신규 등록으로 진행합니다.");
                id = "";
            }
        }
        BodyRecord rec = bm.upsertBodyRecordInteractive(sc, id);
        if (bm.isBmiCalculated(rec)) {
            Ansi.done("[완료] BMI " + String.format("%.1f", rec.getBmi()) + "로 계산되어 저장되었습니다.");
        } else {
            Ansi.alert("[알림] 신장 정보가 없어 BMI를 계산할 수 없습니다.");
            Ansi.alert("       신체 정보를 먼저 등록해 주세요. (EH-05)");
        }
    }
    private void viewBodyHistoryFlow() {
        List<BodyRecord> recs = bm.getAllRecords();
        if (recs.isEmpty()) {
            Ansi.alert("[알림] 등록된 신체 기록이 없습니다.");
            return;
        }
        for (BodyRecord r : recs) {
            String bTxt = bm.isBmiCalculated(r) ? String.format("%.1f", r.getBmi()) : "계산 생략(EH-05)";
            System.out.println("- " + r.getDate() + " | 체중 " + r.getWeight() + "kg | 체지방률 " + r.getBodyFat()
                    + "% | 골격근량 " + r.getMuscleMass() + "kg | BMI " + bTxt);
        }
    }
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
            Ansi.alert("[알림] 포인트가 부족합니다. (보유 " + rm.getCurrentPoint() + "P / 필요 100P) (EH-04)");
            return;
        }
        if (res.miss) {
            Ansi.alert("[알림] 아쉽지만 꽝입니다! 다음 기회에 도전해 보세요.");
        } else {
            Ansi.done("[완료] '" + res.bName + "' 뱃지(" + res.bGrd + " 등급)를 획득했습니다!");
        }
        System.out.println("남은 포인트: " + rm.getCurrentPoint() + " P");
    }
    private void viewBadgesFlow() {
        List<BadgeRecord> owned = rm.getOwnedBadges();
        if (owned.isEmpty()) {
            Ansi.alert("[알림] 보유한 뱃지가 없습니다.");
            return;
        }
        for (BadgeRecord r : owned) {
            System.out.println("- " + rm.getBadgeName(r.getBadgeId()) + " ("
                    + rm.getBadgeGrade(r.getBadgeId()) + ") x" + r.getCount()
                    + " | 최초 획득일: " + r.getFirstObtainedDate());
        }
    }
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
        if (res.total == 0 && res.lines.isEmpty()) {
            Ansi.alert("[알림] 해당 기간에 운동 기록이 없습니다.");
        }
        for (String line : res.lines) System.out.println(line);
        if (res.bOk) {
            System.out.println("체중 변화     : " + res.wLine);
            System.out.println("BMI 변화      : " + res.bmiLine);
        } else {
            Ansi.alert("[알림] 신체 정보가 없어 체중·BMI 통계는 생략합니다. (EH-03)");
        }
    }
}
