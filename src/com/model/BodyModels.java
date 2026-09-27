// UserProfile: n=이름(name), h=신장(height), j=가입일(joinDate)
// BodyRecord: i=id, d=날짜(date), w=체중(weight), f=체지방률(bodyFat), m=골격근량(muscleMass), b=BMI
// 공통: o=JSON객체, p=역직렬화 결과 인스턴스
package com.model;

import com.json.Json.JsonObject;

public final class BodyModels {

    private BodyModels() {
    }

    public static class UserProfile {

        private String n;
        private double h;
        private String j;

        public UserProfile() {
        }

        public UserProfile(String n, double h, String j) {
            this.n = n;
            this.h = h;
            this.j = j;
        }

        public boolean isHeightRegistered() {
            return h > 0;
        }

        public String getName() { return n; }
        public void setName(String n) { this.n = n; }

        public double getHeight() { return h; }
        public void setHeight(double h) { this.h = h; }

        public String getJoinDate() { return j; }

        public JsonObject toJson() {
            return new JsonObject()
                    .put("name", n)
                    .put("height", h)
                    .put("joinDate", j);
        }

        public static UserProfile fromJson(JsonObject o) {
            UserProfile p = new UserProfile();
            p.n = o.getString("name", "사용자");
            p.h = o.getDouble("height", 0);
            p.j = o.getString("joinDate", "");
            return p;
        }
    }

    public static class BodyRecord {

        private String i;
        private String d;
        private double w;
        private double f;
        private double m;
        private double b;

        public BodyRecord() {
        }

        public BodyRecord(String i, String d, double w, double f, double m, double b) {
            this.i = i;
            this.d = d;
            this.w = w;
            this.f = f;
            this.m = m;
            this.b = b;
        }

        public String getId() { return i; }
        public String getDate() { return d; }
        public double getWeight() { return w; }
        public double getBodyFat() { return f; }
        public double getMuscleMass() { return m; }
        public double getBmi() { return b; }
        public void setBmi(double b) { this.b = b; }

        public JsonObject toJson() {
            return new JsonObject()
                    .put("id", i)
                    .put("date", d)
                    .put("weight", w)
                    .put("bodyFat", f)
                    .put("muscleMass", m)
                    .put("bmi", b);
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
}
