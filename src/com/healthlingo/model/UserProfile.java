package com.healthlingo.model;

import com.healthlingo.json.JsonObject;

public class UserProfile {
    private String name;
    private double height;
    private String joinDate;
    public UserProfile() {
    }

    public UserProfile(String name, double height, String joinDate) {
        this.name = name;
        this.height = height;
        this.joinDate = joinDate;
    }
    public boolean isHeightRegistered() {
        return height > 0;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getJoinDate() { return joinDate; }
    public JsonObject toJson() {
        return new JsonObject()
                .put("name", name)
                .put("height", height)
                .put("joinDate", joinDate);
    }
    public static UserProfile fromJson(JsonObject o) {
        UserProfile p = new UserProfile();
        p.name = o.getString("name", "사용자");
        p.height = o.getDouble("height", 0);
        p.joinDate = o.getString("joinDate", "");
        return p;
    }
}
