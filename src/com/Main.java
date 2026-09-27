// s=JsonStorage, p=BodyProfileValidator, c=ExerciseCatalogManager, w=WorkoutManager, b=BodyManager
// n=NotificationManager, r=RewardManager, m=StatisticsManager, u=ConsoleUI, e=예외, a=main인자(args)
package com;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;
import com.manager.BodyManager;
import com.manager.ExerciseCatalogManager;
import com.manager.NotificationManager;
import com.manager.RewardManager;
import com.manager.StatisticsManager;
import com.manager.WorkoutManager;
import com.storage.JsonStorage;
import com.validator.BodyProfileValidator;

public class Main {

    public static void main(String[] a) {
        JsonStorage s = new JsonStorage("data");
        createDataFiles(s);

        BodyProfileValidator p = new BodyProfileValidator();

        ExerciseCatalogManager c = new ExerciseCatalogManager(s);
        WorkoutManager w = new WorkoutManager(s, c);
        BodyManager b = new BodyManager(s, p);
        NotificationManager n = new NotificationManager(s, w);
        RewardManager r = new RewardManager(s);
        StatisticsManager m = new StatisticsManager(w, b, n, p);

        ConsoleUI u = new ConsoleUI(w, b, n, r, m);

        try {
            u.run();
        } catch (Exception e) {
            com.ConsoleUI.alert("[알림] 예기치 못한 오류가 발생하여 프로그램을 종료합니다. (" + e.getMessage() + ")");
        }
    }

    private static void createDataFiles(JsonStorage s) {
        s.ensureFile("user.json", new JsonObject());
        s.ensureFile("body.json", new JsonArray());
        s.ensureFile("exercise.json", new JsonArray());
        s.ensureFile("workout.json", new JsonArray());
        s.ensureFile("routine.json", new JsonArray());
        s.ensureFile("goal.json", new JsonArray());
        s.ensureFile("attendance.json", new JsonObject().put("lastNotifiedDate", "").put("records", new JsonArray()));
        s.ensureFile("points.json", new JsonObject()
                .put("currentPoint", 0).put("totalEarned", 0).put("totalUsed", 0).put("awardedMilestones", new JsonArray()));
        s.ensureFile("badge.json", new JsonObject().put("masters", new JsonArray()).put("records", new JsonArray()));
    }
}
