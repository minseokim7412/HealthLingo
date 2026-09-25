package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

import java.time.LocalDate;
import java.time.YearMonth;

public class Goal {
    private String type;
    private double targetValue;
    private String setDate;
    private String period;
    public Goal() {
    }
    public Goal(String type, double targetValue, String setDate, String period) {
        this.type = type;
        this.targetValue = targetValue;
        this.setDate = setDate;
        this.period = period;
    }
    public String getType() { return type; }
    public double getTargetValue() { return targetValue; }
    public String getSetDate() { return setDate; }
    public String getPeriod() { return period; }

    public boolean isActive(LocalDate today) {
        try {
            return !YearMonth.from(today).isAfter(YearMonth.parse(period));
        } catch (Exception e) {
            return true;
        }
    }
    public JsonObject toJson() {
        return new JsonObject().put("type", type).put("targetValue", targetValue)
                .put("setDate", setDate).put("period", period);
    }
    public static Goal fromJson(JsonObject o) {
        return new Goal(o.getString("type", ""), o.getDouble("targetValue", 0),
                o.getString("setDate", ""), o.getString("period", ""));
    }
}

