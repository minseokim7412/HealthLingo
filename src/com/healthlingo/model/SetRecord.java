package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

/** WorkoutRecord 내부에 포함되는 세트 단위 값 객체 (weight, reps) */
public class SetRecord {

    private final double weight;
    private final int reps;

    public SetRecord(double weight, int reps) {
        this.weight = weight;
        this.reps = reps;
    }

    public double getWeight() { return weight; }
    public int getReps() { return reps; }

    public JsonObject toJson() {
        return new JsonObject().put("weight", weight).put("reps", reps);
    }

    public static SetRecord fromJson(JsonObject o) {
        return new SetRecord(o.getDouble("weight", 0), o.getInt("reps", 0));
    }
}
