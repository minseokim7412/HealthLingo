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

/**
 * MOD-004 NotificationManager (FR-03)
 * 종목별 목표 대비 미달·미출석을 감지하여 하루 1회 경고를 출력한다.
 * attendance.json에 최근 알림 일자(lnd)를 함께 저장하여 하루 1회 제한을 구현한다.
 */
public class NotificationManager {

    private static final String ATTENDANCE_FILE = "attendance.json";
    private static final String GOAL_FILE = "goal.json";

    private final JsonStorage stg;
    private final WorkoutManager wm;
    private final ExerciseCatalogManager cat;
    private final List<AttendanceRecord> atts = new ArrayList<>();
    private final List<Goal> goals = new ArrayList<>();
    private String lnd = "";

    public NotificationManager(JsonStorage stg, WorkoutManager wm, ExerciseCatalogManager cat) {
        this.stg = stg;
        this.wm = wm;
        this.cat = cat;
        load();
    }

    private void load() {
        JsonObject aRoot = stg.loadObject(ATTENDANCE_FILE);
        lnd = aRoot.getString("lastNotifiedDate", "");
        for (Object item : aRoot.getJsonArray("records")) {
            atts.add(AttendanceRecord.fromJson((JsonObject) item));
        }
        for (Object item : stg.loadArray(GOAL_FILE)) {
            goals.add(Goal.fromJson((JsonObject) item));
        }
    }

    public void setGoal(String exerciseName, double targetValue, String period) {
        Goal goal = new Goal(exerciseName.trim(), targetValue, LocalDate.now().toString(), period);
        goals.add(goal);
        saveGoals();
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

    /** 오늘 접속을 출석으로 기록한다. 이미 기록되어 있으면 아무 것도 하지 않는다. */
    public void recordAttendanceToday() {
        if (isAttendedToday()) return;
        atts.add(new AttendanceRecord(LocalDate.now().toString(), true));
        saveAttendance();
    }

    public int getAttendanceCount() {
        int count = 0;
        for (AttendanceRecord r : atts) if (r.isAttended()) count++;
        return count;
    }

    /**
     * 프로그램 시작 직후 자동 호출. 미접속 일수 및 목표 대비 미달을 계산하여
     * 경고 메시지 목록을 반환한다(오늘 이미 안내했다면 빈 목록 대신 요약 메시지만 반환).
     */
    public List<String> checkNotification() {
        List<String> msgs = new ArrayList<>();
        String today = LocalDate.now().toString();

        int days = computeDaysSinceLastAttendance(today);
        boolean gap = days >= 2; // 2일 이상 미접속(장기 미출석) 여부

        List<String> warns = new ArrayList<>();
        List<WorkoutRecord> all = wm.getAllWorkouts();
        for (Goal goal : goals) {
            String exerciseId = cat.findIdByName(goal.getType());
            double best = (exerciseId == null) ? 0 : wm.getMaxWeightForExercise(all, exerciseId);
            if (best < goal.getTargetValue()) {
                warns.add("'" + goal.getType() + "' 목표(" + (long) goal.getTargetValue()
                        + ") 대비 최근 기록이 " + (long) best + "입니다.");
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
        String last = null;
        for (AttendanceRecord r : atts) {
            if (last == null || r.getDate().compareTo(last) > 0) last = r.getDate();
        }
        if (last == null) return 0; // 최초 실행: 정상 진행
        return (int) ChronoUnit.DAYS.between(LocalDate.parse(last), LocalDate.parse(today));
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
