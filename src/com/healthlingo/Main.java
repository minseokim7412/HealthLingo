package com.healthlingo;

import com.healthlingo.manager.BodyManager;
import com.healthlingo.manager.ExerciseCatalogManager;
import com.healthlingo.manager.NotificationManager;
import com.healthlingo.manager.RewardManager;
import com.healthlingo.manager.StatisticsManager;
import com.healthlingo.manager.WorkoutManager;
import com.healthlingo.storage.JsonStorage;
import com.healthlingo.ui.ConsoleUI;
import com.healthlingo.validator.BodyProfileValidator;

/**
 * 헬스링고(Health-Lingo) 진입점.
 * 설계서 3.3절 주요 처리 흐름(프로그램 시작 -> 출석/알림 체크 -> 메인 메뉴 -> 기능 실행 -> 저장 -> 종료)을 따른다.
 */
public class Main {

    public static void main(String[] args) {
        JsonStorage stg = new JsonStorage("data");

        BodyProfileValidator bpv = new BodyProfileValidator();

        ExerciseCatalogManager cat = new ExerciseCatalogManager(stg);
        WorkoutManager wm = new WorkoutManager(stg, cat);
        BodyManager bm = new BodyManager(stg, bpv);
        NotificationManager nm = new NotificationManager(stg, wm, cat);
        RewardManager rm = new RewardManager(stg);
        StatisticsManager sm = new StatisticsManager(wm, bm, cat, nm, bpv);

        ConsoleUI ui = new ConsoleUI(wm, bm, nm, rm, sm, cat);

        // NFR(가용성): 예기치 못한 예외로도 프로그램이 비정상 종료되지 않도록 전역으로 처리한다.
        // 각 Manager가 즉시 저장 원칙을 따르므로, 종료 직전까지의 데이터는 이미 JSON 파일에 반영되어 있다.
        try {
            ui.run();
        } catch (Exception e) {
            System.out.println("[알림] 예기치 못한 오류가 발생하여 프로그램을 종료합니다. (" + e.getMessage() + ")");
        }
    }
}
