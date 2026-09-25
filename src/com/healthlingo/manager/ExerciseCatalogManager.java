package com.healthlingo.manager;

import com.healthlingo.json.JsonArray;
import com.healthlingo.model.ExerciseMaster;
import com.healthlingo.storage.JsonStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MOD-001 ExerciseCatalogManager (FR-01 일부)
 * 자유 입력된 종목명을 검색·정규화하여 종목 ID로 변환하고, 신규 종목이면 자동 등록한다.
 */
public class ExerciseCatalogManager {

    private static final String FILE = "exercise.json";

    private final JsonStorage stg;
    private final List<ExerciseMaster> exs = new ArrayList<>();
    private final Map<String, String> n2id = new HashMap<>(); // 정규화된 이름 -> id, O(1) 검색
    private int seq = 1;

    public ExerciseCatalogManager(JsonStorage stg) {
        this.stg = stg;
        load();
    }

    private void load() {
        JsonArray arr = stg.loadArray(FILE);
        for (Object item : arr) {
            ExerciseMaster e = ExerciseMaster.fromJson((com.healthlingo.json.JsonObject) item);
            exs.add(e);
            n2id.put(normalize(e.getName()), e.getId());
            int num = parseSeq(e.getId());
            if (num >= seq) seq = num + 1;
        }
    }

    private int parseSeq(String id) {
        try {
            return Integer.parseInt(id.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String normalize(String name) {
        return name.trim().toLowerCase().replaceAll("\\s+", "");
    }

    /** 종목명을 받아 종목 ID를 반환한다. 없으면 새로 등록한다. */
    public String resolveOrRegister(String exerciseName) {
        String norm = normalize(exerciseName);
        String eid = n2id.get(norm);
        if (eid != null) {
            return eid;
        }
        String id = "EX" + String.format("%03d", seq++);
        ExerciseMaster nex = new ExerciseMaster(id, exerciseName.trim(), "미분류");
        exs.add(nex);
        n2id.put(norm, id);
        save();
        System.out.println("[알림] 신규 종목 '" + exerciseName.trim() + "'이(가) 자동 등록되었습니다.");
        return id;
    }

    public String getNameById(String exerciseId) {
        for (ExerciseMaster e : exs) {
            if (e.getId().equals(exerciseId)) return e.getName();
        }
        return "알 수 없는 종목";
    }

    public String findIdByName(String exerciseName) {
        return n2id.get(normalize(exerciseName));
    }

    private void save() {
        JsonArray arr = new JsonArray();
        for (ExerciseMaster e : exs) arr.add(e.toJson());
        stg.save(FILE, arr);
    }
}
