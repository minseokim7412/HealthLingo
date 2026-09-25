package com.healthlingo.json;

public class JsonParser {
    private final String text;
    private int pos;
    private JsonParser(String text) {
        this.text = text;
        this.pos = 0;
    }
    public static Object parse(String text) {
        JsonParser p = new JsonParser(text);
        p.skipWhitespace();
        if (p.pos >= p.text.length()) {
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
        JsonObject obj = new JsonObject();
        expect('{');
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return obj;
        }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            Object val = parseValue();
            obj.put(key, val);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
            } else if (c == '}') {
                pos++;
                break;
            } else {
                throw new IllegalArgumentException("JSON 파싱 오류: ',' 또는 '}' 예상 위치 " + pos);
            }
        }
        return obj;
    }
    private JsonArray parseArray() {
        JsonArray arr = new JsonArray();
        expect('[');
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return arr;
        }
        while (true) {
            Object val = parseValue();
            arr.add(val);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
            } else if (c == ']') {
                pos++;
                break;
            } else {
                throw new IllegalArgumentException("JSON 파싱 오류: ',' 또는 ']' 예상 위치 " + pos);
            }
        }
        return arr;
    }
    private String parseString() {
        skipWhitespace();
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = text.charAt(pos++);
            if (c == '"') break;
            if (c == '\\') {
                char esc = text.charAt(pos++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        String hex = text.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    private Double parseNumber() {
        int start = pos;
        while (pos < text.length() && "-+.eE0123456789".indexOf(text.charAt(pos)) >= 0) {
            pos++;
        }
        return Double.parseDouble(text.substring(start, pos));
    }
    private Boolean parseBoolean() {
        if (text.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        } else if (text.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("JSON 파싱 오류: boolean 예상 위치 " + pos);
    }
    private void parseNull() {
        if (text.startsWith("null", pos)) {
            pos += 4;
        } else {
            throw new IllegalArgumentException("JSON 파싱 오류: null 예상 위치 " + pos);
        }
    }
    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }
    private char peek() {
        return text.charAt(pos);
    }
    private void expect(char c) {
        skipWhitespace();
        if (text.charAt(pos) != c) {
            throw new IllegalArgumentException("JSON 파싱 오류: '" + c + "' 예상 위치 " + pos);
        }
        pos++;
    }
}
