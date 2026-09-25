package com.healthlingo.json;

import java.util.Map;

public class JsonWriter {

    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb, 0);
        return sb.toString();
    }

    private static void writeValue(Object value, StringBuilder sb, int ind) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof JsonObject) {
            writeObject((JsonObject) value, sb, ind);
        } else if (value instanceof JsonArray) {
            writeArray((JsonArray) value, sb, ind);
        } else if (value instanceof String) {
            writeString((String) value, sb);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                sb.append((long) d);
            } else {
                sb.append(d);
            }
        } else {
            writeString(String.valueOf(value), sb);
        }
    }

    private static void writeObject(JsonObject obj, StringBuilder sb, int ind) {
        Map<String, Object> map = obj.raw();
        if (map.isEmpty()) {
            sb.append("{}");
            return;
        }
        sb.append("{\n");
        int i = 0;
        int ci = ind + 1;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            indent(sb, ci);
            writeString(e.getKey(), sb);
            sb.append(": ");
            writeValue(e.getValue(), sb, ci);
            if (++i < map.size()) sb.append(',');
            sb.append('\n');
        }
        indent(sb, ind);
        sb.append('}');
    }

    private static void writeArray(JsonArray arr, StringBuilder sb, int ind) {
        if (arr.size() == 0) {
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        int ci = ind + 1;
        for (int i = 0; i < arr.size(); i++) {
            indent(sb, ci);
            writeValue(arr.get(i), sb, ci);
            if (i < arr.size() - 1) sb.append(',');
            sb.append('\n');
        }
        indent(sb, ind);
        sb.append(']');
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append("  ");
    }
}
