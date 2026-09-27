// d=데이터 디렉터리 경로(dir), f=파일명, p=파일 경로(Path), t=파일 내용 텍스트, v=값
// r=반환 결과, j=직렬화된 JSON 문자열, e=예외
package com.storage;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;
import com.json.Json.Parser;
import com.json.Json.Writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonStorage {

    private final Path d;

    public JsonStorage(String p) {
        this.d = Paths.get(p);
        try {
            Files.createDirectories(d);
        } catch (IOException e) {
            com.ConsoleUI.alert("[알림] 데이터 폴더 생성에 실패했습니다: " + e.getMessage());
        }
    }

    private Path fileOf(String f) {
        return d.resolve(f);
    }

    public JsonArray loadArray(String f) {
        Path p = fileOf(f);
        if (!Files.exists(p)) {
            return new JsonArray();
        }
        try {
            String t = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
            if (t.trim().isEmpty()) return new JsonArray();
            Object v = Parser.parse(t);
            JsonArray r = (v instanceof JsonArray) ? (JsonArray) v : new JsonArray();
            System.out.println("[로그] " + f + " 읽기 성공 (" + r.size() + "건)");
            return r;
        } catch (IOException | RuntimeException e) {
            com.ConsoleUI.alert("[알림] " + f + " 읽기에 실패하여 빈 데이터로 시작합니다. (" + e.getMessage() + ")");
            return new JsonArray();
        }
    }

    public JsonObject loadObject(String f) {
        Path p = fileOf(f);
        if (!Files.exists(p)) {
            return new JsonObject();
        }
        try {
            String t = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
            if (t.trim().isEmpty()) return new JsonObject();
            Object v = Parser.parse(t);
            JsonObject r = (v instanceof JsonObject) ? (JsonObject) v : new JsonObject();
            System.out.println("[로그] " + f + " 읽기 성공");
            return r;
        } catch (IOException | RuntimeException e) {
            com.ConsoleUI.alert("[알림] " + f + " 읽기에 실패하여 빈 데이터로 시작합니다. (" + e.getMessage() + ")");
            return new JsonObject();
        }
    }

    public void save(String f, Object v) {
        Path p = fileOf(f);
        try {
            String j = Writer.write(v);
            Files.write(p, j.getBytes(StandardCharsets.UTF_8));
            System.out.println("[로그] " + f + " 저장 성공");
        } catch (IOException e) {
            com.ConsoleUI.alert("[알림] " + f + " 저장에 실패했습니다. 메모리 상의 데이터는 유지됩니다. (" + e.getMessage() + ")");
        }
    }

    public void ensureFile(String f, Object v) {
        if (!Files.exists(fileOf(f))) {
            save(f, v);
        }
    }
}
