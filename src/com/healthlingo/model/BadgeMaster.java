package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

public class BadgeMaster {
    private String id;
    private String name;
    private String grade;
    private double probability;
    public BadgeMaster() {
    }

    public BadgeMaster(String id, String name, String grade, double probability) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.probability = probability;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
    public double getProbability() { return probability; }
    public boolean isMiss() { return "NONE".equals(id); }
    public JsonObject toJson() {
        return new JsonObject().put("id", id).put("name", name).put("grade", grade).put("probability", probability);
    }
    public static BadgeMaster fromJson(JsonObject o) {
        return new BadgeMaster(o.getString("id", ""), o.getString("name", ""),
                o.getString("grade", ""), o.getDouble("probability", 0));
    }
}
