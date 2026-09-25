package com.healthlingo.json;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonObject {
    private final Map<String, Object> map = new LinkedHashMap<>();
    public JsonObject put(String key, Object value) {
        map.put(key, value);
        return this;
    }
    public boolean has(String key) {
        return map.containsKey(key) && map.get(key) != null;
    }
    public String getString(String key, String def) {
        Object v = map.get(key);
        return v == null ? def : String.valueOf(v);
    }
    public double getDouble(String key, double def) {
        Object v = map.get(key);
        if (v == null) return def;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
    public int getInt(String key, int def) {
        return (int) Math.round(getDouble(key, def));
    }
    public boolean getBoolean(String key, boolean def) {
        Object v = map.get(key);
        if (v == null) return def;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(String.valueOf(v));
    }
    public JsonObject getJsonObject(String key) {
        Object v = map.get(key);
        return (v instanceof JsonObject) ? (JsonObject) v : null;
    }
    public JsonArray getJsonArray(String key) {
        Object v = map.get(key);
        if (v instanceof JsonArray) return (JsonArray) v;
        return new JsonArray();
    }
    public Map<String, Object> raw() {
        return map;
    }
}
