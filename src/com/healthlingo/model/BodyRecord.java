package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

/**
 * body.json - 신체 변화 이력
 * PK: id
 */
public class BodyRecord {

    private String id;
    private String date;
    private double weight;
    private double bodyFat;
    private double muscleMass;
    private double bmi; // 신장 미등록(EH-05) 시 0으로 계산 생략

    public BodyRecord() {
    }

    public BodyRecord(String id, String date, double weight, double bodyFat, double muscleMass, double bmi) {
        this.id = id;
        this.date = date;
        this.weight = weight;
        this.bodyFat = bodyFat;
        this.muscleMass = muscleMass;
        this.bmi = bmi;
    }

    public String getId() { return id; }
    public String getDate() { return date; }
    public double getWeight() { return weight; }
    public double getBodyFat() { return bodyFat; }
    public double getMuscleMass() { return muscleMass; }
    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("date", date)
                .put("weight", weight)
                .put("bodyFat", bodyFat)
                .put("muscleMass", muscleMass)
                .put("bmi", bmi);
    }

    public static BodyRecord fromJson(JsonObject o) {
        return new BodyRecord(
                o.getString("id", ""),
                o.getString("date", ""),
                o.getDouble("weight", 0),
                o.getDouble("bodyFat", 0),
                o.getDouble("muscleMass", 0),
                o.getDouble("bmi", 0)
        );
    }
}
