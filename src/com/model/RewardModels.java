// PointsAccount: c=현재포인트(currentPoint), e=누적적립(totalEarned), u=누적사용(totalUsed), f=마일스톤달성플래그(flags)
// BadgeMaster: i=id, n=이름(name), g=등급(grade), p=확률(probability)
// BadgeRecord: i=뱃지ID(badgeId), c=보유 개수(count), d=최초 획득일(firstObtainedDate)
// 공통: a=JsonArray 임시값, k=키, o=JSON객체, x=순회 임시값
package com.model;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;

import java.util.LinkedHashSet;
import java.util.Set;

public final class RewardModels {

    private RewardModels() {
    }

    public static class PointsAccount {

        private int c;
        private int e;
        private int u;
        private final Set<String> f = new LinkedHashSet<>();

        public int getCurrentPoint() { return c; }
        public int getTotalEarned() { return e; }
        public int getTotalUsed() { return u; }

        public void earn(int a) {
            c += a;
            e += a;
        }

        public boolean use(int a) {
            if (c < a) return false;
            c -= a;
            u += a;
            return true;
        }

        public boolean isMilestoneAwarded(String k) {
            return f.contains(k);
        }

        public void markMilestoneAwarded(String k) {
            f.add(k);
        }

        public JsonObject toJson() {
            JsonArray a = new JsonArray();
            for (String k : f) a.add(k);
            return new JsonObject()
                    .put("currentPoint", c)
                    .put("totalEarned", e)
                    .put("totalUsed", u)
                    .put("awardedMilestones", a);
        }

        public static PointsAccount fromJson(JsonObject o) {
            PointsAccount p = new PointsAccount();
            p.c = o.getInt("currentPoint", 0);
            p.e = o.getInt("totalEarned", 0);
            p.u = o.getInt("totalUsed", 0);
            for (Object x : o.getJsonArray("awardedMilestones")) {
                p.f.add(String.valueOf(x));
            }
            return p;
        }
    }

    public static class BadgeMaster {

        private String i;
        private String n;
        private String g;
        private double p;

        public BadgeMaster() {
        }

        public BadgeMaster(String i, String n, String g, double p) {
            this.i = i;
            this.n = n;
            this.g = g;
            this.p = p;
        }

        public String getId() { return i; }
        public String getName() { return n; }
        public String getGrade() { return g; }
        public double getProbability() { return p; }
        public boolean isMiss() { return "NONE".equals(i); }

        public JsonObject toJson() {
            return new JsonObject().put("id", i).put("name", n).put("grade", g).put("probability", p);
        }

        public static BadgeMaster fromJson(JsonObject o) {
            return new BadgeMaster(o.getString("id", ""), o.getString("name", ""),
                    o.getString("grade", ""), o.getDouble("probability", 0));
        }
    }

    public static class BadgeRecord {

        private String i;
        private int c;
        private String d;

        public BadgeRecord() {
        }

        public BadgeRecord(String i, int c, String d) {
            this.i = i;
            this.c = c;
            this.d = d;
        }

        public String getBadgeId() { return i; }
        public int getCount() { return c; }
        public void increment() { c++; }
        public String getFirstObtainedDate() { return d; }

        public JsonObject toJson() {
            return new JsonObject().put("badgeId", i).put("count", c).put("firstObtainedDate", d);
        }

        public static BadgeRecord fromJson(JsonObject o) {
            return new BadgeRecord(o.getString("badgeId", ""), o.getInt("count", 0), o.getString("firstObtainedDate", ""));
        }
    }
}
