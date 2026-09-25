package com.healthlingo.manager;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.model.Routine;
import com.healthlingo.model.SetRecord;
import com.healthlingo.model.WorkoutRecord;
import com.healthlingo.storage.JsonStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MOD-002 WorkoutManager (FR-01, EH-01, EH-02)
 * 종목명 자유 입력 처리, 날짜별 운동 기록 및 루틴 등록·조회를 담당한다.
 * 설계서 9장(비기능요구사항): 날짜 키 Map을 사용해 데이터가 누적되어도 O(1)에 가깝게 조회한다.
 */
public class WorkoutManager {

    private static final String WORKOUT_FILE = "workout.json";
    private static final String ROUTINE_FILE = "routine.json";

    private final JsonStorage stg;
    private final ExerciseCatalogManager cat;
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

    /** 종목명 자유 입력을 받아 이미 형식 검증된 날짜·세트 목록으로 운동 기록을 등록한다. (FR-01) */
    public WorkoutRecord addWorkout(String date, String exerciseName, List<SetRecord> sets) {
        String exerciseId = cat.resolveOrRegister(exerciseName);
        String id = "W" + String.format("%04d", wSeq++);
        WorkoutRecord rec = new WorkoutRecord(id, date, exerciseId, sets);
        recs.add(rec);
        byDate.computeIfAbsent(date, d -> new ArrayList<>()).add(rec);
        saveWorkouts();
        return rec;
    }

    /** 날짜별 조회. EH-02: 기록이 없으면 안내 메시지를 출력하고 빈 리스트를 반환해 메뉴로 복귀시킨다. */
    public List<WorkoutRecord> getWorkoutsByDate(String date) {
        List<WorkoutRecord> res = byDate.getOrDefault(date, new ArrayList<>());
        if (res.isEmpty()) {
            System.out.println("[알림] 해당 날짜(" + date + ")의 운동 기록이 없습니다. (EH-02)");
        }
        return res;
    }

    public List<WorkoutRecord> getAllWorkouts() {
        return recs;
    }

    public int getWorkoutCount() {
        return recs.size();
    }

    /** 종목명 기준 최고 무게(기간 필터는 호출자가 리스트를 넘겨 계산) */
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
