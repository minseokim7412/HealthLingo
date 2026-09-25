package com.healthlingo.manager;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.model.Routine;
import com.healthlingo.model.SetRecord;
import com.healthlingo.model.WorkoutRecord;
import com.healthlingo.storage.JsonStorage;
import com.healthlingo.validator.InputValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WorkoutManager {
    private static final String WORKOUT_FILE = "workout.json";
    private static final String ROUTINE_FILE = "routine.json";
    private final JsonStorage stg;
    private final ExerciseCatalogManager cat;
    private final InputValidator val = new InputValidator();
    private final List<WorkoutRecord> recs = new ArrayList<>();
    private final Map<String, List<WorkoutRecord>> byDate = new HashMap<>();
    private final List<Routine> rts = new ArrayList<>();
    private int wSeq = 1;
    private int rSeq = 1;
    public WorkoutManager(JsonStorage stg, ExerciseCatalogManager cat) {
        this.stg = stg;
        this.cat = cat;
        load();
    }

    private void load() {
        for (Object item : stg.loadArray(WORKOUT_FILE)) {
            WorkoutRecord r = WorkoutRecord.fromJson((JsonObject) item);
            recs.add(r);
            byDate.computeIfAbsent(r.getDate(), d -> new ArrayList<>()).add(r);
            int n = parseSeq(r.getId());
            if (n >= wSeq) wSeq = n + 1;
        }
        for (Object item : stg.loadArray(ROUTINE_FILE)) {
            Routine r = Routine.fromJson((JsonObject) item);
            rts.add(r);
            int n = parseSeq(r.getId());
            if (n >= rSeq) rSeq = n + 1;
        }
    }
    private int parseSeq(String id) {
        try {
            return Integer.parseInt(id.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public WorkoutRecord addWorkout(String date, String exerciseName, List<SetRecord> sets) {
        String exerciseId = cat.resolveOrRegister(exerciseName);
        String id = "W" + String.format("%04d", wSeq++);
        WorkoutRecord rec = new WorkoutRecord(id, date, exerciseId, sets);
        recs.add(rec);
        byDate.computeIfAbsent(date, d -> new ArrayList<>()).add(rec);
        saveWorkouts();
        return rec;
    }
    public WorkoutRecord addWorkoutInteractive(Scanner sc) {
        String exerciseName = val.readNonEmpty(sc, "종목명을 입력하세요 >> ");
        int nSet = val.readPositiveInt(sc, "세트 수를 입력하세요 >> ");
        List<SetRecord> sets = new ArrayList<>();
        for (int i = 1; i <= nSet; i++) {
            double weight = val.readNonNegativeDouble(sc, i + "세트 무게(kg) >> ");
            int reps = val.readPositiveInt(sc, i + "세트 횟수 >> ");
            sets.add(new SetRecord(weight, reps));
        }
        return addWorkout(LocalDate.now().toString(), exerciseName, sets);
    }
    public List<WorkoutRecord> getWorkoutsByDate(String date) {
        List<WorkoutRecord> res = byDate.getOrDefault(date, new ArrayList<>());
        if (res.isEmpty()) {
            com.healthlingo.ui.Ansi.alert("[알림] 해당 날짜(" + date + ")의 운동 기록이 없습니다. (EH-02)");
        }
        return res;
    }
    public List<WorkoutRecord> getWorkoutsByDateInteractive(Scanner sc) {
        String date = val.readDate(sc, "조회할 날짜를 입력하세요 (yyyy-MM-dd) >> ");
        return getWorkoutsByDate(date);
    }

    public String getExerciseName(String exerciseId) {
        return cat.getNameById(exerciseId);
    }
    public String resolveExerciseId(String exerciseName) {
        return cat.resolveOrRegister(exerciseName);
    }
    public List<WorkoutRecord> getAllWorkouts() {
        return recs;
    }
    public int getWorkoutCount() {
        return recs.size();
    }
    public double getMaxWeightForExercise(List<WorkoutRecord> scope, String exerciseId) {
        double max = 0;
        for (WorkoutRecord r : scope) {
            if (r.getExerciseId().equals(exerciseId)) max = Math.max(max, r.getMaxWeight());
        }
        return max;
    }
    public Routine addRoutine(String name, String cycle, List<String> names) {
        List<String> ids = new ArrayList<>();
        for (String nm : names) ids.add(cat.resolveOrRegister(nm));
        String id = "R" + String.format("%03d", rSeq++);
        Routine rt = new Routine(id, name, cycle, ids);
        rts.add(rt);
        saveRoutines();
        return rt;
    }
    public Routine addRoutineInteractive(Scanner sc) {
        String name = val.readNonEmpty(sc, "루틴 이름 >> ");
        String cycle = val.readNonEmpty(sc, "적용 요일/주기 (예: 월,수,금) >> ");
        int count = val.readPositiveInt(sc, "루틴에 포함할 종목 수 >> ");
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= count; i++) names.add(val.readNonEmpty(sc, i + "번째 종목명 >> "));
        return addRoutine(name, cycle, names);
    }
    public List<Routine> getRoutines() {
        return rts;
    }
    private void saveWorkouts() {
        JsonArray arr = new JsonArray();
        for (WorkoutRecord r : recs) arr.add(r.toJson());
        stg.save(WORKOUT_FILE, arr);
    }

    private void saveRoutines() {
        JsonArray arr = new JsonArray();
        for (Routine r : rts) arr.add(r.toJson());
        stg.save(ROUTINE_FILE, arr);
    }
}
