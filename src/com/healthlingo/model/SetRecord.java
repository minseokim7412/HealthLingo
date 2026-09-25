package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

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

