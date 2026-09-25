package com.healthlingo.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonArray implements Iterable<Object> {

    private final List<Object> arr = new ArrayList<>();

    public JsonArray add(Object value) {
        arr.add(value);
        return this;
    }

    public int size() {
        return arr.size();
    }

    public Object get(int index) {
        return arr.get(index);
    }

    public JsonObject getJsonObject(int index) {
        Object v = arr.get(index);
        return (v instanceof JsonObject) ? (JsonObject) v : null;
    }

    public String getString(int index) {
        return String.valueOf(arr.get(index));
    }

    @Override
    public Iterator<Object> iterator() {
        return arr.iterator();
    }

    public List<Object> raw() {
        return arr;
    }
}

