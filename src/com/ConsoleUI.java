// c=Scanner, v=InputValidator, w=WorkoutManager, b=BodyManager, n=NotificationManager, r=RewardManager, m=StatisticsManager
// g=메시지목록/입력값(문맥별), o=반복플래그/신체정보유효여부(문맥별), k=메뉴선택값/임시결과객체(문맥별), s=순회문자열
// i=순회변수(WorkoutRecord/Routine/BodyRecord/String 등), d=StringBuilder/기록ID/기간Enum(문맥별), x=인덱스/카운트(문맥별)
// alert/done/prompt: msg=출력 메시지 (원래 Ansi 클래스, 콘솔 색상 담당이라 ConsoleUI로 합침)
package com;

import com.manager.BodyManager;
import com.manager.NotificationManager;
import com.manager.RewardManager;
import com.manager.StatisticsManager;
import com.manager.WorkoutManager;
import com.model.RewardModels.BadgeRecord;
import com.model.BodyModels.BodyRecord;
import com.model.WorkoutModels.Routine;
import com.model.WorkoutModels.SetRecord;
import com.model.BodyModels.UserProfile;
import com.model.WorkoutModels.WorkoutRecord;
import com.validator.InputValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private static final String LINE = "===================================";
    private static final String SUB_LINE = "-----------------------------------";

    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";

    public static void alert(String msg) {
        System.err.println(YELLOW + msg + RESET);
    }

    public static void done(String msg) {
        System.out.println(GREEN + msg + RESET);
    }

    public static void prompt(String msg) {
        System.out.print(CYAN + msg + RESET);
    }

    private final Scanner c = new Scanner(System.in);
    private final InputValidator v = new InputValidator();

    private final WorkoutManager w;
    private final BodyManager b;
    private final NotificationManager n;
    private final RewardManager r;
    private final StatisticsManager m;

    public ConsoleUI(WorkoutManager w, BodyManager b, NotificationManager n,
                      RewardManager r, StatisticsManager m) {
        this.w = w;
        this.b = b;
        this.n = n;
        this.r = r;
        this.m = m;
    }

    public void run() {
        ensureProfile();

        List<String> g = n.checkNotification();
        n.recordAttendanceToday();
        r.checkMilestones("ATTEND", n.getAttendanceCount());

        boolean o = true;
        while (o) {
            o = showMainMenu(g);
            g = new ArrayList<>();
        }
        done("[완료] 데이터가 저장되었습니다. 헬스링고를 종료합니다.");
    }

    private void ensureProfile() {
        UserProfile p = b.getProfile();
        if (p != null) return;

        System.out.println(LINE);
        System.out.println(" 헬스링고 (Health-Lingo) 최초 실행");
        System.out.println(LINE);
        b.registerProfileInteractive(c);
        done("[완료] 프로필이 등록되었습니다.\n");
    }

    private boolean showMainMenu(List<String> g) {
        System.out.println();
        System.out.println(LINE);
        System.out.println(" 헬스링고 (Health-Lingo)");
        System.out.println(LINE);
        for (String s : g) {
            if (s.startsWith("[알림]")) alert(s);
            else System.out.println(s);
        }
        System.out.println(SUB_LINE);
        System.out.println("1. 운동 기록 및 루틴");
        System.out.println("2. 신체 변화 기록");
        System.out.println("3. 뱃지 · 보상");
        System.out.println("4. 통계 보기");
        System.out.println("5. 종료");
        int k = v.readMenuChoice(c, "번호를 입력하세요 >> ", 1, 5);

        switch (k) {
            case 1: workoutMenu(); break;
            case 2: bodyMenu(); break;
            case 3: rewardMenu(); break;
            case 4: statsMenu(); break;
            case 5: return false;
        }
        return true;
    }

    private void workoutMenu() {
        boolean o = true;
        while (o) {
            System.out.println();
            System.out.println(LINE);
            System.out.println(" 운동 기록 및 루틴");
            System.out.println(LINE);
            System.out.println("1. 오늘 운동 등록하기");
            System.out.println("2. 날짜별 운동 기록 조회");
            System.out.println("3. 루틴 확인하기 / 추가하기");
            System.out.println("4. 목표 설정하기");
            System.out.println("0. 이전 메뉴로");
            int k = v.readMenuChoice(c, "번호를 입력하세요 >> ", 0, 4);
            switch (k) {
                case 1: addWorkoutFlow(); break;
                case 2: viewWorkoutByDateFlow(); break;
                case 3: routineFlow(); break;
                case 4: setGoalFlow(); break;
                case 0: o = false; break;
            }
        }
    }

    private void addWorkoutFlow() {
        WorkoutRecord k = w.addWorkoutInteractive(c);
        r.checkMilestones("WORKOUT", w.getWorkoutCount());
        done("[완료] " + k.getDate() + " 운동 기록이 저장되었습니다.");
    }

    private void viewWorkoutByDateFlow() {
        List<WorkoutRecord> k = w.getWorkoutsByDateInteractive(c);
        if (k.isEmpty()) {
            return;
        }
        System.out.println("[" + k.get(0).getDate() + " 운동 기록]");
        for (WorkoutRecord i : k) {
            StringBuilder d = new StringBuilder("- 종목: " + w.getExerciseName(i.getExerciseId()) + " | 세트: ");
            for (SetRecord s : i.getSets()) d.append(s.getWeight()).append("kg x ").append(s.getReps()).append("회  ");
            System.out.println(d);
        }
    }

    private void routineFlow() {
        List<Routine> k = w.getRoutines();
        if (k.isEmpty()) {
            alert("[알림] 등록된 루틴이 없습니다.");
        } else {
            System.out.println("[등록된 루틴]");
            for (Routine i : k) {
                System.out.println("- " + i.getName() + " (" + i.getCycle() + ") 종목 수: " + i.getExerciseIds().size());
            }
        }
        int x = v.readMenuChoice(c, "루틴을 추가하시겠습니까? (1: 예, 0: 아니오) >> ", 0, 1);
        if (x == 1) {
            w.addRoutineInteractive(c);
            done("[완료] 루틴이 등록되었습니다.");
        }
    }

    private void setGoalFlow() {
        String k = v.readNonEmpty(c, "목표를 설정할 종목명 >> ");
        double x = v.readPositiveDouble(c, "목표 무게(kg) >> ");
        String d = v.readNonEmpty(c, "적용 기간(예: 2026-09) >> ");
        n.setGoal(k, x, d);
        done("[완료] 목표가 설정되었습니다.");
    }

    private void bodyMenu() {
        boolean o = true;
        while (o) {
            System.out.println();
            System.out.println(LINE);
            System.out.println(" 신체 변화 기록");
            System.out.println(LINE);
            System.out.println("1. 신체 정보 등록/수정");
            System.out.println("2. BMI 및 이력 조회");
            System.out.println("0. 이전 메뉴로");
            int k = v.readMenuChoice(c, "번호를 입력하세요 >> ", 0, 2);
            switch (k) {
                case 1: registerBodyFlow(); break;
                case 2: viewBodyHistoryFlow(); break;
                case 0: o = false; break;
            }
        }
    }

    private void registerBodyFlow() {
        String k = "";
        List<BodyRecord> x = b.getAllRecords();
        if (!x.isEmpty()) {
            System.out.println("[기존 신체 기록]");
            for (BodyRecord i : x) {
                System.out.println("- " + i.getId() + " | " + i.getDate() + " | " + i.getWeight() + "kg");
            }
            k = v.readOptional(c, "수정할 기록 ID (신규 등록은 그냥 Enter) >> ");
            if (!k.isEmpty() && b.findById(k) == null) {
                alert("[알림] 해당 ID의 기록이 없어 신규 등록으로 진행합니다.");
                k = "";
            }
        }

        BodyRecord d = b.upsertBodyRecordInteractive(c, k);

        if (b.isBmiCalculated(d)) {
            done("[완료] BMI " + String.format("%.1f", d.getBmi()) + "로 계산되어 저장되었습니다.");
        } else {
            alert("[알림] 신장 정보가 없어 BMI를 계산할 수 없습니다.");
            alert("       신체 정보를 먼저 등록해 주세요. (EH-05)");
        }
    }

    private void viewBodyHistoryFlow() {
        List<BodyRecord> k = b.getAllRecords();
        if (k.isEmpty()) {
            alert("[알림] 등록된 신체 기록이 없습니다.");
            return;
        }
        for (BodyRecord i : k) {
            String x = b.isBmiCalculated(i) ? String.format("%.1f", i.getBmi()) : "계산 생략(EH-05)";
            System.out.println("- " + i.getDate() + " | 체중 " + i.getWeight() + "kg | 체지방률 " + i.getBodyFat()
                    + "% | 골격근량 " + i.getMuscleMass() + "kg | BMI " + x);
        }
    }

    private void rewardMenu() {
        boolean o = true;
        while (o) {
            System.out.println();
            System.out.println(LINE);
            System.out.println(" 뱃지 · 보상");
            System.out.println(LINE);
            System.out.println("보유 포인트: " + r.getCurrentPoint() + " P");
            System.out.println("1. 뱃지 뽑기 (100P 소모)");
            System.out.println("2. 보유 뱃지 목록 보기");
            System.out.println("0. 이전 메뉴로");
            int k = v.readMenuChoice(c, "번호를 입력하세요 >> ", 0, 2);
            switch (k) {
                case 1: drawBadgeFlow(); break;
                case 2: viewBadgesFlow(); break;
                case 0: o = false; break;
            }
        }
    }

    private void drawBadgeFlow() {
        RewardManager.DrawResult k = r.drawBadge();
        if (!k.o) {
            alert("[알림] 포인트가 부족합니다. (보유 " + r.getCurrentPoint() + "P / 필요 100P) (EH-04)");
            return;
        }
        if (k.m) {
            alert("[알림] 아쉽지만 꽝입니다! 다음 기회에 도전해 보세요.");
        } else {
            done("[완료] '" + k.n + "' 뱃지(" + k.g + " 등급)를 획득했습니다!");
        }
        System.out.println("남은 포인트: " + r.getCurrentPoint() + " P");
    }

    private void viewBadgesFlow() {
        List<BadgeRecord> k = r.getOwnedBadges();
        if (k.isEmpty()) {
            alert("[알림] 보유한 뱃지가 없습니다.");
            return;
        }
        for (BadgeRecord i : k) {
            System.out.println("- " + r.getBadgeName(i.getBadgeId()) + " ("
                    + r.getBadgeGrade(i.getBadgeId()) + ") x" + i.getCount()
                    + " | 최초 획득일: " + i.getFirstObtainedDate());
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
        int x = v.readMenuChoice(c, "번호를 입력하세요 >> ", 0, 4);
        if (x == 0) return;

        StatisticsManager.Period d;
        switch (x) {
            case 1: d = StatisticsManager.Period.DAILY; break;
            case 2: d = StatisticsManager.Period.WEEKLY; break;
            case 3: d = StatisticsManager.Period.MONTHLY; break;
            default: d = StatisticsManager.Period.ALL; break;
        }

        StatisticsManager.StatResult k = m.getStatistics(d);
        System.out.println();
        System.out.println("[" + k.l + " 통계]");
        System.out.println("총 운동 횟수 : " + k.t + "회");
        if (k.t == 0 && k.i.isEmpty()) {
            alert("[알림] 해당 기간에 운동 기록이 없습니다.");
        }
        for (String i : k.i) System.out.println(i);
        if (k.o) {
            System.out.println("체중 변화     : " + k.w);
            System.out.println("BMI 변화      : " + k.m);
        } else {
            alert("[알림] 신체 정보가 없어 체중·BMI 통계는 생략합니다. (EH-03)");
        }
    }
}
