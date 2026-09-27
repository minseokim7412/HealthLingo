// Goal: t=목표 종목ID(type), v=목표값(targetValue), d=설정일(setDate), p=적용기간(period), y=오늘 날짜(isActive 파라미터)
// AttendanceRecord: d=날짜(date), a=출석 여부(attended)
// 공통: o=JSON객체
package com.model;

import com.json.Json.JsonObject;

import java.time.LocalDate;
import java.time.YearMonth;

public final class NotificationModels {

    private NotificationModels() {
    }

    public static class Goal {

        private String t;
        private double v;
        private String d;
        private String p;

        public Goal() {
        }

        public Goal(String t, double v, String d, String p) {
            this.t = t;
            this.v = v;
            this.d = d;
            this.p = p;
        }

        public String getType() { return t; }
        public double getTargetValue() { return v; }
        public String getSetDate() { return d; }
        public String getPeriod() { return p; }

        public boolean isActive(LocalDate y) {
            try {
                return !YearMonth.from(y).isAfter(YearMonth.parse(p));
            } catch (Exception e) {
                return true;
            }
        }

        public JsonObject toJson() {
            return new JsonObject().put("type", t).put("targetValue", v)
                    .put("setDate", d).put("period", p);
        }

        public static Goal fromJson(JsonObject o) {
            return new Goal(o.getString("type", ""), o.getDouble("targetValue", 0),
                    o.getString("setDate", ""), o.getString("period", ""));
        }
    }

    public static class AttendanceRecord {

        private String d;
        private boolean a;

        public AttendanceRecord() {
        }

        public AttendanceRecord(String d, boolean a) {
            this.d = d;
            this.a = a;
        }

        public String getDate() { return d; }
        public boolean isAttended() { return a; }

        public JsonObject toJson() {
            return new JsonObject().put("date", d).put("attended", a);
        }

        public static AttendanceRecord fromJson(JsonObject o) {
            return new AttendanceRecord(o.getString("date", ""), o.getBoolean("attended", false));
        }
    }
}
