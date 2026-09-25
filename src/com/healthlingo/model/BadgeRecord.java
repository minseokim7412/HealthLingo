package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

/**
 * badge.json 내 "records" 배열 - 뱃지 보유 현황
 * PK: badgeId (BadgeMaster FK)
 */
public class BadgeRecord {

    private String badgeId;
    private int count;
    private String firstObtainedDate;

    public BadgeRecord() {
    }

    public BadgeRecord(String badgeId, int count, String firstObtainedDate) {
        this.badgeId = badgeId;
        this.count = count;
        this.firstObtainedDate = firstObtainedDate;
    }

    public String getBadgeId() { return badgeId; }
    public int getCount() { return count; }
    public void increment() { count++; }
    public String getFirstObtainedDate() { return firstObtainedDate; }

    public JsonObject toJson() {
        return new JsonObject().put("badgeId", badgeId).put("count", count).put("firstObtainedDate", firstObtainedDate);
    }

    public static BadgeRecord fromJson(JsonObject o) {
        return new BadgeRecord(o.getString("badgeId", ""), o.getInt("count", 0), o.getString("firstObtainedDate", ""));
    }
}
