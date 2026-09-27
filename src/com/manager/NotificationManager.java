// s=JsonStorage, w=WorkoutManager, a=출석목록(atts), g=목표목록(goals), l=마지막알림일(lnd), d=마지막출석일(lastAttDate)
// j=JSON임시변수, i=순회변수, r=AttendanceRecord임시변수, n=일수/카운트, k=Goal임시변수, e=종목ID
// y=오늘LocalDate/날짜문자열, z=오늘날짜문자열/순회문자열, m=메시지목록, b=미출석여부, u=경고목록, v=전체운동기록, x=최고기록/인덱스, o=경고발생여부
package com.manager;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;
import com.model.NotificationModels.AttendanceRecord;
import com.model.NotificationModels.Goal;
import com.model.WorkoutModels.WorkoutRecord;
import com.storage.JsonStorage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private static final String ATTENDANCE_FILE = "attendance.json";
    private static final String GOAL_FILE = "goal.json";

    private final JsonStorage s;
    private final WorkoutManager w;
    private final List<AttendanceRecord> a = new ArrayList<>();
    private final List<Goal> g = new ArrayList<>();
    private String l = "";
    private String d = null;

    public NotificationManager(JsonStorage s, WorkoutManager w) {
        this.s = s;
        this.w = w;
        load();
    }

    private void load() {
        JsonObject j = s.loadObject(ATTENDANCE_FILE);
        l = j.getString("lastNotifiedDate", "");
        for (Object i : j.getJsonArray("records")) {
            AttendanceRecord r = AttendanceRecord.fromJson((JsonObject) i);
            a.add(r);
            if (d == null || r.getDate().compareTo(d) > 0) d = r.getDate();
        }
        for (Object i : s.loadArray(GOAL_FILE)) {
            g.add(Goal.fromJson((JsonObject) i));
        }
    }

    public void setGoal(String n, double v, String p) {
        String e = w.resolveExerciseId(n);
        String y = LocalDate.now().toString();
        Goal k = new Goal(e, v, y, p);
        int x = indexOfGoal(e, y);
        if (x >= 0) {
            g.set(x, k);
        } else {
            g.add(k);
        }
        saveGoals();
    }

    private int indexOfGoal(String e, String y) {
        for (int i = 0; i < g.size(); i++) {
            Goal k = g.get(i);
            if (k.getType().equals(e) && k.getSetDate().equals(y)) return i;
        }
        return -1;
    }

    public List<Goal> getGoals() {
        return g;
    }

    public boolean isAttendedToday() {
        String y = LocalDate.now().toString();
        for (AttendanceRecord r : a) {
            if (r.getDate().equals(y)) return true;
        }
        return false;
    }

    public List<AttendanceRecord> getAttendanceRecords() {
        return a;
    }

    public void recordAttendanceToday() {
        if (isAttendedToday()) return;
        String y = LocalDate.now().toString();
        a.add(new AttendanceRecord(y, true));
        d = y;
        saveAttendance();
    }

    public int getAttendanceCount() {
        int n = 0;
        for (AttendanceRecord r : a) if (r.isAttended()) n++;
        return n;
    }

    public List<String> checkNotification() {
        List<String> m = new ArrayList<>();
        LocalDate y = LocalDate.now();
        String z = y.toString();

        int n = computeDaysSinceLastAttendance(z);
        boolean b = n >= 2;

        List<String> u = new ArrayList<>();
        List<WorkoutRecord> v = w.getAllWorkouts();
        for (Goal k : g) {
            if (!k.isActive(y)) continue;
            String e = k.getType();
            double x = w.getMaxWeightForExercise(v, e);
            if (x < k.getTargetValue()) {
                u.add("'" + w.getExerciseName(e) + "' 목표(" + (long) k.getTargetValue()
                        + "kg) 대비 최근 기록이 " + (long) x + "kg입니다.");
            }
        }

        boolean o = b || !u.isEmpty();
        if (o && !z.equals(l)) {
            if (b) {
                m.add("[알림] " + n + "일째 접속하지 않았어요! 오늘 운동 어때요?");
            }
            m.addAll(prefixAlerts(u));
            l = z;
            saveAttendance();
        } else if (o) {
            m.add("[알림] 오늘 이미 목표/출석 안내를 확인했습니다. 계속 화이팅하세요!");
        } else {
            m.add("정상적으로 운동을 이어가고 있습니다. 오늘도 화이팅!");
        }
        return m;
    }

    private List<String> prefixAlerts(List<String> u) {
        List<String> m = new ArrayList<>();
        for (String z : u) m.add("[알림] " + z);
        return m;
    }

    private int computeDaysSinceLastAttendance(String z) {
        if (d == null) return 0;
        return (int) ChronoUnit.DAYS.between(LocalDate.parse(d), LocalDate.parse(z));
    }

    private void saveAttendance() {
        JsonArray j = new JsonArray();
        for (AttendanceRecord r : a) j.add(r.toJson());
        JsonObject k = new JsonObject().put("lastNotifiedDate", l).put("records", j);
        s.save(ATTENDANCE_FILE, k);
    }

    private void saveGoals() {
        JsonArray j = new JsonArray();
        for (Goal k : g) j.add(k.toJson());
        s.save(GOAL_FILE, j);
    }
}
