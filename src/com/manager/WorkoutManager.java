// s=JsonStorage, c=ExerciseCatalogManager, v=InputValidator, r=운동기록목록(recs), y=날짜별기록Map(byDate)
// t=루틴목록(rts), w=운동기록ID발급순번(wSeq), q=루틴ID발급순번(rSeq), o=Scanner, d=날짜(date)
// n=이름(종목명/루틴명), e=종목ID(exerciseId), l=목록(세트/종목ID/이름), k=신규생성레코드
// i=순회변수, u=개수/생성된Routine, g=주기(cycle), z=람다/순회임시변수, m=최고무게, p=횟수(reps)
package com.manager;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;
import com.model.WorkoutModels.Routine;
import com.model.WorkoutModels.SetRecord;
import com.model.WorkoutModels.WorkoutRecord;
import com.storage.JsonStorage;
import com.validator.InputValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WorkoutManager {

    private static final String WORKOUT_FILE = "workout.json";
    private static final String ROUTINE_FILE = "routine.json";

    private final JsonStorage s;
    private final ExerciseCatalogManager c;
    private final InputValidator v = new InputValidator();
    private final List<WorkoutRecord> r = new ArrayList<>();
    private final Map<String, List<WorkoutRecord>> y = new HashMap<>();
    private final List<Routine> t = new ArrayList<>();
    private int w = 1;
    private int q = 1;

    public WorkoutManager(JsonStorage s, ExerciseCatalogManager c) {
        this.s = s;
        this.c = c;
        load();
    }

    private void load() {
        for (Object i : s.loadArray(WORKOUT_FILE)) {
            WorkoutRecord k = WorkoutRecord.fromJson((JsonObject) i);
            r.add(k);
            y.computeIfAbsent(k.getDate(), z -> new ArrayList<>()).add(k);
            int n = parseSeq(k.getId());
            if (n >= w) w = n + 1;
        }
        for (Object i : s.loadArray(ROUTINE_FILE)) {
            Routine u = Routine.fromJson((JsonObject) i);
            t.add(u);
            int n = parseSeq(u.getId());
            if (n >= q) q = n + 1;
        }
    }

    private int parseSeq(String i) {
        try {
            return Integer.parseInt(i.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public WorkoutRecord addWorkout(String d, String n, List<SetRecord> l) {
        String e = c.resolveOrRegister(n);
        String i = "W" + String.format("%04d", w++);
        WorkoutRecord k = new WorkoutRecord(i, d, e, l);
        r.add(k);
        y.computeIfAbsent(d, z -> new ArrayList<>()).add(k);
        saveWorkouts();
        return k;
    }

    public WorkoutRecord addWorkoutInteractive(Scanner o) {
        String n = v.readNonEmpty(o, "종목명을 입력하세요 >> ");
        int u = v.readPositiveInt(o, "세트 수를 입력하세요 >> ");
        List<SetRecord> l = new ArrayList<>();
        for (int i = 1; i <= u; i++) {
            double g = v.readNonNegativeDouble(o, i + "세트 무게(kg) >> ");
            int p = v.readPositiveInt(o, i + "세트 횟수 >> ");
            l.add(new SetRecord(g, p));
        }
        return addWorkout(LocalDate.now().toString(), n, l);
    }

    public List<WorkoutRecord> getWorkoutsByDate(String d) {
        List<WorkoutRecord> k = y.getOrDefault(d, new ArrayList<>());
        if (k.isEmpty()) {
            com.ConsoleUI.alert("[알림] 해당 날짜(" + d + ")의 운동 기록이 없습니다. (EH-02)");
        }
        return k;
    }

    public List<WorkoutRecord> getWorkoutsByDateInteractive(Scanner o) {
        String d = v.readDate(o, "조회할 날짜를 입력하세요 (yyyy-MM-dd) >> ");
        return getWorkoutsByDate(d);
    }

    public String getExerciseName(String e) {
        return c.getNameById(e);
    }

    public String resolveExerciseId(String n) {
        return c.resolveOrRegister(n);
    }

    public List<WorkoutRecord> getAllWorkouts() {
        return r;
    }

    public int getWorkoutCount() {
        return r.size();
    }

    public double getMaxWeightForExercise(List<WorkoutRecord> k, String e) {
        double m = 0;
        for (WorkoutRecord i : k) {
            if (i.getExerciseId().equals(e)) m = Math.max(m, i.getMaxWeight());
        }
        return m;
    }

    public Routine addRoutine(String n, String g, List<String> l) {
        List<String> k = new ArrayList<>();
        for (String z : l) k.add(c.resolveOrRegister(z));
        String i = "R" + String.format("%03d", q++);
        Routine u = new Routine(i, n, g, k);
        t.add(u);
        saveRoutines();
        return u;
    }

    public Routine addRoutineInteractive(Scanner o) {
        String n = v.readNonEmpty(o, "루틴 이름 >> ");
        String g = v.readNonEmpty(o, "적용 요일/주기 (예: 월,수,금) >> ");
        int u = v.readPositiveInt(o, "루틴에 포함할 종목 수 >> ");
        List<String> l = new ArrayList<>();
        for (int i = 1; i <= u; i++) l.add(v.readNonEmpty(o, i + "번째 종목명 >> "));
        return addRoutine(n, g, l);
    }

    public List<Routine> getRoutines() {
        return t;
    }

    private void saveWorkouts() {
        JsonArray a = new JsonArray();
        for (WorkoutRecord i : r) a.add(i.toJson());
        s.save(WORKOUT_FILE, a);
    }

    private void saveRoutines() {
        JsonArray a = new JsonArray();
        for (Routine i : t) a.add(i.toJson());
        s.save(ROUTINE_FILE, a);
    }
}
