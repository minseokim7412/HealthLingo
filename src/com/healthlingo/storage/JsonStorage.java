package com.healthlingo.storage;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.json.JsonParser;
import com.healthlingo.json.JsonWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonStorage {
    private final Path dir;
    public JsonStorage(String p) {
        this.dir = Paths.get(p);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            com.healthlingo.ui.Ansi.alert("[알림] 데이터 폴더 생성에 실패했습니다: " + e.getMessage());
        }
    }
    private Path fileOf(String file) {
        return dir.resolve(file);
    }
    public JsonArray loadArray(String file) {
        Path path = fileOf(file);
        if (!Files.exists(path)) {
            return new JsonArray();
        }
        try {
            String txt = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            if (txt.trim().isEmpty()) return new JsonArray();
            Object val = JsonParser.parse(txt);
            JsonArray res = (val instanceof JsonArray) ? (JsonArray) val : new JsonArray();
            System.out.println("[로그] " + file + " 읽기 성공 (" + res.size() + "건)");
            return res;
        } catch (IOException | RuntimeException e) {
            com.healthlingo.ui.Ansi.alert("[알림] " + file + " 읽기에 실패하여 빈 데이터로 시작합니다. (" + e.getMessage() + ")");
            return new JsonArray();
        }
    }
    public JsonObject loadObject(String file) {
        Path path = fileOf(file);
        if (!Files.exists(path)) {
            return new JsonObject();
        }
        try {
            String txt = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            if (txt.trim().isEmpty()) return new JsonObject();
            Object val = JsonParser.parse(txt);
            JsonObject res = (val instanceof JsonObject) ? (JsonObject) val : new JsonObject();
            System.out.println("[로그] " + file + " 읽기 성공");
            return res;
        } catch (IOException | RuntimeException e) {
            com.healthlingo.ui.Ansi.alert("[알림] " + file + " 읽기에 실패하여 빈 데이터로 시작합니다. (" + e.getMessage() + ")");
            return new JsonObject();
        }
    }
    public void save(String file, Object value) {
        Path path = fileOf(file);
        try {
            String json = JsonWriter.write(value);
            Files.write(path, json.getBytes(StandardCharsets.UTF_8));
            System.out.println("[로그] " + file + " 저장 성공");
        } catch (IOException e) {
            com.healthlingo.ui.Ansi.alert("[알림] " + file + " 저장에 실패했습니다. 메모리 상의 데이터는 유지됩니다. (" + e.getMessage() + ")");
        }
    }
    public void ensureFile(String file, Object emptyValue) {
        if (!Files.exists(fileOf(file))) {
            save(file, emptyValue);
        }
    }
}
