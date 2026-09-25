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

/**
 * MOD-009 JsonStorage
 * 9개 데이터 클래스의 공통 파일 입출력을 담당한다.
 * 파일이 없으면(최초 실행) 빈 데이터로 초기화하고, 쓰기 실패 시에도
 * 예외를 잡아 프로그램이 종료되지 않도록 한다. (설계 원칙: 즉시 저장)
 */
public class JsonStorage {

    private final Path dir;

    public JsonStorage(String p) {
        this.dir = Paths.get(p);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.out.println("[알림] 데이터 폴더 생성에 실패했습니다: " + e.getMessage());
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
            return (val instanceof JsonArray) ? (JsonArray) val : new JsonArray();
        } catch (IOException | RuntimeException e) {
            System.out.println("[알림] " + file + " 읽기에 실패하여 빈 데이터로 시작합니다. (" + e.getMessage() + ")");
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
            return (val instanceof JsonObject) ? (JsonObject) val : new JsonObject();
        } catch (IOException | RuntimeException e) {
            System.out.println("[알림] " + file + " 읽기에 실패하여 빈 데이터로 시작합니다. (" + e.getMessage() + ")");
            return new JsonObject();
        }
    }

    public void save(String file, Object value) {
        Path path = fileOf(file);
        try {
            String json = JsonWriter.write(value);
            Files.write(path, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("[알림] " + file + " 저장에 실패했습니다. 메모리 상의 데이터는 유지됩니다. (" + e.getMessage() + ")");
        }
    }
}
