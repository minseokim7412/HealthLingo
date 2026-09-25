package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

public class ExerciseMaster {
    private String id;
    private String name;
    private String bodyPart;
    public ExerciseMaster() {
    }

    public ExerciseMaster(String id, String name, String bodyPart) {
        this.id = id;
        this.name = name;
        this.bodyPart = bodyPart;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getBodyPart() { return bodyPart; }
    public JsonObject toJson() {
        return new JsonObject().put("id", id).put("name", name).put("bodyPart", bodyPart);
    }
    public static ExerciseMaster fromJson(JsonObject o) {
        return new ExerciseMaster(o.getString("id", ""), o.getString("name", ""), o.getString("bodyPart", "미분류"));
    }
}
