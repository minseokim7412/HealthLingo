package com.healthlingo.model;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRecord {
    private String id;
    private String date;
    private String exerciseId;
    private List<SetRecord> sets;
    public WorkoutRecord() {
    }

    public WorkoutRecord(String id, String date, String exerciseId, List<SetRecord> sets) {
        this.id = id;
        this.date = date;
        this.exerciseId = exerciseId;
        this.sets = sets;
    }
    public String getId() { return id; }
    public String getDate() { return date; }
    public String getExerciseId() { return exerciseId; }
    public List<SetRecord> getSets() { return sets; }
    public double getMaxWeight() {
        double max = 0;
        for (SetRecord s : sets) max = Math.max(max, s.getWeight());
        return max;
    }
    public JsonObject toJson() {
        JsonArray arr = new JsonArray();
        for (SetRecord s : sets) arr.add(s.toJson());
        return new JsonObject()
                .put("id", id)
                .put("date", date)
                .put("exerciseId", exerciseId)
                .put("sets", arr);
    }
    public static WorkoutRecord fromJson(JsonObject o) {
        List<SetRecord> sets = new ArrayList<>();
        for (Object item : o.getJsonArray("sets")) {
            sets.add(SetRecord.fromJson((JsonObject) item));
        }
        return new WorkoutRecord(o.getString("id", ""), o.getString("date", ""), o.getString("exerciseId", ""), sets);
    }
}
