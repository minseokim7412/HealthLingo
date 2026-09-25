package com.healthlingo.model;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;

import java.util.LinkedHashSet;
import java.util.Set;

public class PointsAccount {
    private int currentPoint;
    private int totalEarned;
    private int totalUsed;
    private final Set<String> flags = new LinkedHashSet<>();
    public int getCurrentPoint() { return currentPoint; }
    public int getTotalEarned() { return totalEarned; }
    public int getTotalUsed() { return totalUsed; }

    public void earn(int amt) {
        currentPoint += amt;
        totalEarned += amt;
    }
    public boolean use(int amt) {
        if (currentPoint < amt) return false;
        currentPoint -= amt;
        totalUsed += amt;
        return true;
    }
    public boolean isMilestoneAwarded(String key) {
        return flags.contains(key);
    }

    public void markMilestoneAwarded(String key) {
        flags.add(key);
    }

    public JsonObject toJson() {
        JsonArray arr = new JsonArray();
        for (String k : flags) arr.add(k);
        return new JsonObject()
                .put("currentPoint", currentPoint)
                .put("totalEarned", totalEarned)
                .put("totalUsed", totalUsed)
                .put("awardedMilestones", arr);
    }
    public static PointsAccount fromJson(JsonObject o) {
        PointsAccount p = new PointsAccount();
        p.currentPoint = o.getInt("currentPoint", 0);
        p.totalEarned = o.getInt("totalEarned", 0);
        p.totalUsed = o.getInt("totalUsed", 0);
        for (Object item : o.getJsonArray("awardedMilestones")) {
            p.flags.add(String.valueOf(item));
        }
        return p;
    }
}
