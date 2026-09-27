// m=내부 저장 Map, k=키(key), v=값(value), d=기본값(default) (JsonObject)
// a=내부 리스트, i=인덱스 (JsonArray)
// t=원본 문자열(text), o=읽는 위치(pos), p=파서 인스턴스, j=JsonObject 임시값, c=현재 문자, b=문자열 버퍼, x=이스케이프 문자, h=유니코드 4자리, s=숫자 시작 위치 (Parser)
// n=들여쓰기 레벨, e=Map.Entry (Writer)
package com.json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

    private Json() {
    }

    public static final class JsonObject {

        private final Map<String, Object> m = new LinkedHashMap<>();

        public JsonObject put(String k, Object v) {
            m.put(k, v);
            return this;
        }

        public boolean has(String k) {
            return m.containsKey(k) && m.get(k) != null;
        }

        public String getString(String k, String d) {
            Object v = m.get(k);
            return v == null ? d : String.valueOf(v);
        }

        public double getDouble(String k, double d) {
            Object v = m.get(k);
            if (v == null) return d;
            if (v instanceof Number) return ((Number) v).doubleValue();
            try {
                return Double.parseDouble(String.valueOf(v));
            } catch (NumberFormatException e) {
                return d;
            }
        }

        public int getInt(String k, int d) {
            return (int) Math.round(getDouble(k, d));
        }

        public boolean getBoolean(String k, boolean d) {
            Object v = m.get(k);
            if (v == null) return d;
            if (v instanceof Boolean) return (Boolean) v;
            return Boolean.parseBoolean(String.valueOf(v));
        }

        public JsonObject getJsonObject(String k) {
            Object v = m.get(k);
            return (v instanceof JsonObject) ? (JsonObject) v : null;
        }

        public JsonArray getJsonArray(String k) {
            Object v = m.get(k);
            if (v instanceof JsonArray) return (JsonArray) v;
            return new JsonArray();
        }

        public Map<String, Object> raw() {
            return m;
        }
    }

    public static final class JsonArray implements Iterable<Object> {

        private final List<Object> a = new ArrayList<>();

        public JsonArray add(Object v) {
            a.add(v);
            return this;
        }

        public int size() {
            return a.size();
        }

        public Object get(int i) {
            return a.get(i);
        }

        public JsonObject getJsonObject(int i) {
            Object v = a.get(i);
            return (v instanceof JsonObject) ? (JsonObject) v : null;
        }

        public String getString(int i) {
            return String.valueOf(a.get(i));
        }

        @Override
        public Iterator<Object> iterator() {
            return a.iterator();
        }

        public List<Object> raw() {
            return a;
        }
    }

    public static final class Parser {

        private final String t;
        private int o;

        private Parser(String t) {
            this.t = t;
            this.o = 0;
        }

        public static Object parse(String t) {
            Parser p = new Parser(t);
            p.skipWhitespace();
            if (p.o >= p.t.length()) {
                return new JsonObject();
            }
            return p.parseValue();
        }

        private Object parseValue() {
            skipWhitespace();
            char c = peek();
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArray();
                case '"': return parseString();
                case 't':
                case 'f': return parseBoolean();
                case 'n': parseNull(); return null;
                default: return parseNumber();
            }
        }

        private JsonObject parseObject() {
            JsonObject j = new JsonObject();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                o++;
                return j;
            }
            while (true) {
                skipWhitespace();
                String k = parseString();
                skipWhitespace();
                expect(':');
                Object v = parseValue();
                j.put(k, v);
                skipWhitespace();
                char c = peek();
                if (c == ',') {
                    o++;
                } else if (c == '}') {
                    o++;
                    break;
                } else {
                    throw new IllegalArgumentException("JSON 파싱 오류: ',' 또는 '}' 예상 위치 " + o);
                }
            }
            return j;
        }

        private JsonArray parseArray() {
            JsonArray a = new JsonArray();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                o++;
                return a;
            }
            while (true) {
                Object v = parseValue();
                a.add(v);
                skipWhitespace();
                char c = peek();
                if (c == ',') {
                    o++;
                } else if (c == ']') {
                    o++;
                    break;
                } else {
                    throw new IllegalArgumentException("JSON 파싱 오류: ',' 또는 ']' 예상 위치 " + o);
                }
            }
            return a;
        }

        private String parseString() {
            skipWhitespace();
            expect('"');
            StringBuilder b = new StringBuilder();
            while (true) {
                char c = t.charAt(o++);
                if (c == '"') break;
                if (c == '\\') {
                    char x = t.charAt(o++);
                    switch (x) {
                        case '"': b.append('"'); break;
                        case '\\': b.append('\\'); break;
                        case '/': b.append('/'); break;
                        case 'n': b.append('\n'); break;
                        case 't': b.append('\t'); break;
                        case 'r': b.append('\r'); break;
                        case 'b': b.append('\b'); break;
                        case 'f': b.append('\f'); break;
                        case 'u':
                            String h = t.substring(o, o + 4);
                            b.append((char) Integer.parseInt(h, 16));
                            o += 4;
                            break;
                        default: b.append(x);
                    }
                } else {
                    b.append(c);
                }
            }
            return b.toString();
        }

        private Double parseNumber() {
            int s = o;
            while (o < t.length() && "-+.eE0123456789".indexOf(t.charAt(o)) >= 0) {
                o++;
            }
            return Double.parseDouble(t.substring(s, o));
        }

        private Boolean parseBoolean() {
            if (t.startsWith("true", o)) {
                o += 4;
                return Boolean.TRUE;
            } else if (t.startsWith("false", o)) {
                o += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("JSON 파싱 오류: boolean 예상 위치 " + o);
        }

        private void parseNull() {
            if (t.startsWith("null", o)) {
                o += 4;
            } else {
                throw new IllegalArgumentException("JSON 파싱 오류: null 예상 위치 " + o);
            }
        }

        private void skipWhitespace() {
            while (o < t.length() && Character.isWhitespace(t.charAt(o))) {
                o++;
            }
        }

        private char peek() {
            return t.charAt(o);
        }

        private void expect(char c) {
            skipWhitespace();
            if (t.charAt(o) != c) {
                throw new IllegalArgumentException("JSON 파싱 오류: '" + c + "' 예상 위치 " + o);
            }
            o++;
        }
    }

    public static final class Writer {

        public static String write(Object v) {
            StringBuilder b = new StringBuilder();
            writeValue(v, b, 0);
            return b.toString();
        }

        private static void writeValue(Object v, StringBuilder b, int n) {
            if (v == null) {
                b.append("null");
            } else if (v instanceof JsonObject) {
                writeObject((JsonObject) v, b, n);
            } else if (v instanceof JsonArray) {
                writeArray((JsonArray) v, b, n);
            } else if (v instanceof String) {
                writeString((String) v, b);
            } else if (v instanceof Boolean) {
                b.append(v);
            } else if (v instanceof Number) {
                double d = ((Number) v).doubleValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    b.append((long) d);
                } else {
                    b.append(d);
                }
            } else {
                writeString(String.valueOf(v), b);
            }
        }

        private static void writeObject(JsonObject j, StringBuilder b, int n) {
            Map<String, Object> m = j.raw();
            if (m.isEmpty()) {
                b.append("{}");
                return;
            }
            b.append("{\n");
            int i = 0;
            int c = n + 1;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                indent(b, c);
                writeString(e.getKey(), b);
                b.append(": ");
                writeValue(e.getValue(), b, c);
                if (++i < m.size()) b.append(',');
                b.append('\n');
            }
            indent(b, n);
            b.append('}');
        }

        private static void writeArray(JsonArray a, StringBuilder b, int n) {
            if (a.size() == 0) {
                b.append("[]");
                return;
            }
            b.append("[\n");
            int c = n + 1;
            for (int i = 0; i < a.size(); i++) {
                indent(b, c);
                writeValue(a.get(i), b, c);
                if (i < a.size() - 1) b.append(',');
                b.append('\n');
            }
            indent(b, n);
            b.append(']');
        }

        private static void writeString(String s, StringBuilder b) {
            b.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"': b.append("\\\""); break;
                    case '\\': b.append("\\\\"); break;
                    case '\n': b.append("\\n"); break;
                    case '\t': b.append("\\t"); break;
                    case '\r': b.append("\\r"); break;
                    default:
                        if (c < 0x20) {
                            b.append(String.format("\\u%04x", (int) c));
                        } else {
                            b.append(c);
                        }
                }
            }
            b.append('"');
        }

        private static void indent(StringBuilder b, int n) {
            for (int i = 0; i < n; i++) b.append("  ");
        }
    }
}
