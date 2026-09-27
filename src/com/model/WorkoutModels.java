// SetRecord: w=무게(weight), r=횟수(reps)
// ExerciseMaster: i=id, n=종목명(name), b=부위(bodyPart)
// Routine: i=id, n=이름(name), c=주기(cycle), e=종목ID 목록(exerciseIds)
// WorkoutRecord: i=id, d=날짜(date), e=종목ID(exerciseId), l=세트 목록(sets), m=최고 무게, s=SetRecord 순회 변수
// 공통: a=JsonArray 임시값, x=순회 임시값, o=JSON객체
package com.model;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class WorkoutModels {

    private WorkoutModels() {
    }

    public static class SetRecord {

        private final double w;
        private final int r;

        public SetRecord(double w, int r) {
            this.w = w;
            this.r = r;
        }

        public double getWeight() { return w; }
        public int getReps() { return r; }

        public JsonObject toJson() {
            return new JsonObject().put("weight", w).put("reps", r);
        }

        public static SetRecord fromJson(JsonObject o) {
            return new SetRecord(o.getDouble("weight", 0), o.getInt("reps", 0));
        }
    }

    public static class ExerciseMaster {

        private String i;
        private String n;
        private String b;

        public ExerciseMaster() {
        }

        public ExerciseMaster(String i, String n, String b) {
            this.i = i;
            this.n = n;
            this.b = b;
        }

        public String getId() { return i; }
        public String getName() { return n; }
        public String getBodyPart() { return b; }

        public JsonObject toJson() {
            return new JsonObject().put("id", i).put("name", n).put("bodyPart", b);
        }

        public static ExerciseMaster fromJson(JsonObject o) {
            return new ExerciseMaster(o.getString("id", ""), o.getString("name", ""), o.getString("bodyPart", "미분류"));
        }
    }

    public static class Routine {

        private String i;
        private String n;
        private String c;
        private List<String> e;

        public Routine() {
        }

        public Routine(String i, String n, String c, List<String> e) {
            this.i = i;
            this.n = n;
            this.c = c;
            this.e = e;
        }

        public String getId() { return i; }
        public String getName() { return n; }
        public String getCycle() { return c; }
        public List<String> getExerciseIds() { return e; }

        public JsonObject toJson() {
            JsonArray a = new JsonArray();
            for (String x : e) a.add(x);
            return new JsonObject().put("id", i).put("name", n).put("cycle", c).put("exerciseIds", a);
        }

        public static Routine fromJson(JsonObject o) {
            List<String> e = new ArrayList<>();
            for (Object x : o.getJsonArray("exerciseIds")) e.add(String.valueOf(x));
            return new Routine(o.getString("id", ""), o.getString("name", ""), o.getString("cycle", ""), e);
        }
    }

    public static class WorkoutRecord {

        private String i;
        private String d;
        private String e;
        private List<SetRecord> l;

        public WorkoutRecord() {
        }

        public WorkoutRecord(String i, String d, String e, List<SetRecord> l) {
            this.i = i;
            this.d = d;
            this.e = e;
            this.l = l;
        }

        public String getId() { return i; }
        public String getDate() { return d; }
        public String getExerciseId() { return e; }
        public List<SetRecord> getSets() { return l; }

        public double getMaxWeight() {
            double m = 0;
            for (SetRecord s : l) m = Math.max(m, s.getWeight());
            return m;
        }

        public JsonObject toJson() {
            JsonArray a = new JsonArray();
            for (SetRecord s : l) a.add(s.toJson());
            return new JsonObject()
                    .put("id", i)
                    .put("date", d)
                    .put("exerciseId", e)
                    .put("sets", a);
        }

        public static WorkoutRecord fromJson(JsonObject o) {
            List<SetRecord> l = new ArrayList<>();
            for (Object x : o.getJsonArray("sets")) {
                l.add(SetRecord.fromJson((JsonObject) x));
            }
            return new WorkoutRecord(o.getString("id", ""), o.getString("date", ""), o.getString("exerciseId", ""), l);
        }
    }
}
