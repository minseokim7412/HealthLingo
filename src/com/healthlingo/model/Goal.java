package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

/**
 * goal.json - 종목별 목표 설정
 * PK: type + setDate
 * type: 목표를 적용할 운동 종목명(자유 입력 종목명과 동일 문자열)
 */
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

    public JsonObject toJson() {
        return new JsonObject().put("type", type).put("targetValue", targetValue)
                .put("setDate", setDate).put("period", period);
    }

    public static Goal fromJson(JsonObject o) {
        return new Goal(o.getString("type", ""), o.getDouble("targetValue", 0),
                o.getString("setDate", ""), o.getString("period", ""));
    }
}
