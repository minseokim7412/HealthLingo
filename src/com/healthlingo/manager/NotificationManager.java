package com.healthlingo.manager;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.model.AttendanceRecord;
import com.healthlingo.model.Goal;
import com.healthlingo.model.WorkoutRecord;
import com.healthlingo.storage.JsonStorage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static final String ATTENDANCE_FILE = "attendance.json";
    private static final String GOAL_FILE = "goal.json";
    private final JsonStorage stg;
    private final WorkoutManager wm;
    private final List<AttendanceRecord> atts = new ArrayList<>();
    private final List<Goal> goals = new ArrayList<>();
    private String lnd = "";
    private String lastAttDate = null;
    public NotificationManager(JsonStorage stg, WorkoutManager wm) {
        this.stg = stg;
        this.wm = wm;
        load();
    }

    private void load() {
        JsonObject aRoot = stg.loadObject(ATTENDANCE_FILE);
        lnd = aRoot.getString("lastNotifiedDate", "");
        for (Object item : aRoot.getJsonArray("records")) {
            AttendanceRecord r = AttendanceRecord.fromJson((JsonObject) item);
            atts.add(r);
            if (lastAttDate == null || r.getDate().compareTo(lastAttDate) > 0) lastAttDate = r.getDate();
        }
        for (Object item : stg.loadArray(GOAL_FILE)) {
            goals.add(Goal.fromJson((JsonObject) item));
        }
    }
    public void setGoal(String exerciseName, double targetValue, String period) {
        String exerciseId = wm.resolveExerciseId(exerciseName);
        String today = LocalDate.now().toString();
        Goal goal = new Goal(exerciseId, targetValue, today, period);
        int idx = indexOfGoal(exerciseId, today);
        if (idx >= 0) {
            goals.set(idx, goal);
        } else {
            goals.add(goal);
        }
        saveGoals();
    }
    private int indexOfGoal(String exerciseId, String setDate) {
        for (int i = 0; i < goals.size(); i++) {
            Goal g = goals.get(i);
            if (g.getType().equals(exerciseId) && g.getSetDate().equals(setDate)) return i;
        }
        return -1;
    }
    public List<Goal> getGoals() {
        return goals;
    }
    public boolean isAttendedToday() {
        String today = LocalDate.now().toString();
        for (AttendanceRecord r : atts) {
            if (r.getDate().equals(today)) return true;
        }
        return false;
    }
    public List<AttendanceRecord> getAttendanceRecords() {
        return atts;
    }
    public void recordAttendanceToday() {
        if (isAttendedToday()) return;
        String today = LocalDate.now().toString();
        atts.add(new AttendanceRecord(today, true));
        lastAttDate = today;
        saveAttendance();
    }

    public int getAttendanceCount() {
        int count = 0;
        for (AttendanceRecord r : atts) if (r.isAttended()) count++;
        return count;
    }
    public List<String> checkNotification() {
        List<String> msgs = new ArrayList<>();
        LocalDate now = LocalDate.now();
        String today = now.toString();
        int days = computeDaysSinceLastAttendance(today);
        boolean gap = days >= 2;

        List<String> warns = new ArrayList<>();
        List<WorkoutRecord> all = wm.getAllWorkouts();
        for (Goal goal : goals) {
            if (!goal.isActive(now)) continue;
            String exerciseId = goal.getType();
            double best = wm.getMaxWeightForExercise(all, exerciseId);
            if (best < goal.getTargetValue()) {
                warns.add("'" + wm.getExerciseName(exerciseId) + "' 목표(" + (long) goal.getTargetValue()
                        + "kg) 대비 최근 기록이 " + (long) best + "kg입니다.");
            }
        }

        boolean warn = gap || !warns.isEmpty();
        if (warn && !today.equals(lnd)) {
            if (gap) {
                msgs.add("[알림] " + days + "일째 접속하지 않았어요! 오늘 운동 어때요?");
            }
            msgs.addAll(prefixAlerts(warns));
            lnd = today;
            saveAttendance();
        } else if (warn) {
            msgs.add("[알림] 오늘 이미 목표/출석 안내를 확인했습니다. 계속 화이팅하세요!");
        } else {
            msgs.add("정상적으로 운동을 이어가고 있습니다. 오늘도 화이팅!");
        }
        return msgs;
    }
    private List<String> prefixAlerts(List<String> raw) {
        List<String> res = new ArrayList<>();
        for (String s : raw) res.add("[알림] " + s);
        return res;
    }
    private int computeDaysSinceLastAttendance(String today) {
        if (lastAttDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(LocalDate.parse(lastAttDate), LocalDate.parse(today));
    }

    private void saveAttendance() {
        JsonArray arr = new JsonArray();
        for (AttendanceRecord r : atts) arr.add(r.toJson());
        JsonObject root = new JsonObject().put("lastNotifiedDate", lnd).put("records", arr);
        stg.save(ATTENDANCE_FILE, root);
    }
    private void saveGoals() {
        JsonArray arr = new JsonArray();
        for (Goal g : goals) arr.add(g.toJson());
        stg.save(GOAL_FILE, arr);
    }
}
