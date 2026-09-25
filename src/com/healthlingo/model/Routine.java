package com.healthlingo.model;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * routine.json - 요일별 루틴
 * PK: id, exerciseIds는 ExerciseMaster(FK, N:M)
 */
public class Routine {

    private String id;
    private String name;
    private String cycle;
    private List<String> exerciseIds;

    public Routine() {
    }

    public Routine(String id, String name, String cycle, List<String> exerciseIds) {
        this.id = id;
        this.name = name;
        this.cycle = cycle;
        this.exerciseIds = exerciseIds;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCycle() { return cycle; }
    public List<String> getExerciseIds() { return exerciseIds; }

    public JsonObject toJson() {
        JsonArray arr = new JsonArray();
        for (String id : exerciseIds) arr.add(id);
        return new JsonObject().put("id", id).put("name", name).put("cycle", cycle).put("exerciseIds", arr);
    }

    public static Routine fromJson(JsonObject o) {
        List<String> ids = new ArrayList<>();
        for (Object item : o.getJsonArray("exerciseIds")) ids.add(String.valueOf(item));
        return new Routine(o.getString("id", ""), o.getString("name", ""), o.getString("cycle", ""), ids);
    }
}
