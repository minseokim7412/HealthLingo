package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

/**
 * attendance.json - 접속/출석 이력
 * PK: date
 */
public class AttendanceRecord {

    private String date;
    private boolean attended;

    public AttendanceRecord() {
    }

    public AttendanceRecord(String date, boolean attended) {
        this.date = date;
        this.attended = attended;
    }

    public String getDate() { return date; }
    public boolean isAttended() { return attended; }

    public JsonObject toJson() {
        return new JsonObject().put("date", date).put("attended", attended);
    }

    public static AttendanceRecord fromJson(JsonObject o) {
        return new AttendanceRecord(o.getString("date", ""), o.getBoolean("attended", false));
    }
}
