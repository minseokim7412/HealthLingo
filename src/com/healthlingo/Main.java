package com.healthlingo;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.manager.BodyManager;
import com.healthlingo.manager.ExerciseCatalogManager;
import com.healthlingo.manager.NotificationManager;
import com.healthlingo.manager.RewardManager;
import com.healthlingo.manager.StatisticsManager;
import com.healthlingo.manager.WorkoutManager;
import com.healthlingo.storage.JsonStorage;
import com.healthlingo.ui.ConsoleUI;
import com.healthlingo.validator.BodyProfileValidator;

public class Main {
    public static void main(String[] args) {
        JsonStorage stg = new JsonStorage("data");
        createDataFiles(stg);
        BodyProfileValidator bpv = new BodyProfileValidator();
        ExerciseCatalogManager cat = new ExerciseCatalogManager(stg);
        WorkoutManager wm = new WorkoutManager(stg, cat);
        BodyManager bm = new BodyManager(stg, bpv);
        NotificationManager nm = new NotificationManager(stg, wm);
        RewardManager rm = new RewardManager(stg);
        StatisticsManager sm = new StatisticsManager(wm, bm, nm, bpv);
        ConsoleUI ui = new ConsoleUI(wm, bm, nm, rm, sm);

        try {
            ui.run();
        } catch (Exception e) {
            com.healthlingo.ui.Ansi.alert("[알림] 예기치 못한 오류가 발생하여 프로그램을 종료합니다. (" + e.getMessage() + ")");
        }
    }
    private static void createDataFiles(JsonStorage stg) {
        stg.ensureFile("user.json", new JsonObject());
        stg.ensureFile("body.json", new JsonArray());
        stg.ensureFile("exercise.json", new JsonArray());
        stg.ensureFile("workout.json", new JsonArray());
        stg.ensureFile("routine.json", new JsonArray());
        stg.ensureFile("goal.json", new JsonArray());
        stg.ensureFile("attendance.json", new JsonObject().put("lastNotifiedDate", "").put("records", new JsonArray()));
        stg.ensureFile("points.json", new JsonObject()
                .put("currentPoint", 0).put("totalEarned", 0).put("totalUsed", 0).put("awardedMilestones", new JsonArray()));
        stg.ensureFile("badge.json", new JsonObject().put("masters", new JsonArray()).put("records", new JsonArray()));
    }
}
