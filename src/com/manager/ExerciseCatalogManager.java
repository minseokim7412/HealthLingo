// s=JsonStorage, x=종목마스터 목록(exs), m=이름→ID 매핑(n2id), q=ID 발급 순번(seq)
// a=JsonArray 임시값, i=순회변수/신규ID, e=ExerciseMaster 임시값, u=파싱된 순번, n=종목명, z=정규화된 이름, d=기존 ID
package com.manager;

import com.json.Json.JsonArray;
import com.model.WorkoutModels.ExerciseMaster;
import com.storage.JsonStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExerciseCatalogManager {

    private static final String FILE = "exercise.json";

    private final JsonStorage s;
    private final List<ExerciseMaster> x = new ArrayList<>();
    private final Map<String, String> m = new HashMap<>();
    private int q = 1;

    public ExerciseCatalogManager(JsonStorage s) {
        this.s = s;
        load();
    }

    private void load() {
        JsonArray a = s.loadArray(FILE);
        for (Object i : a) {
            ExerciseMaster e = ExerciseMaster.fromJson((com.json.Json.JsonObject) i);
            x.add(e);
            m.put(normalize(e.getName()), e.getId());
            int u = parseSeq(e.getId());
            if (u >= q) q = u + 1;
        }
    }

    private int parseSeq(String i) {
        try {
            return Integer.parseInt(i.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String normalize(String n) {
        return n.trim().toLowerCase().replaceAll("\\s+", "");
    }

    public String resolveOrRegister(String n) {
        String z = normalize(n);
        String d = m.get(z);
        if (d != null) {
            return d;
        }
        String i = "EX" + String.format("%03d", q++);
        ExerciseMaster e = new ExerciseMaster(i, n.trim(), "미분류");
        x.add(e);
        m.put(z, i);
        save();
        com.ConsoleUI.alert("[알림] 신규 종목 '" + n.trim() + "'이(가) 자동 등록되었습니다.");
        return i;
    }

    public String getNameById(String i) {
        for (ExerciseMaster e : x) {
            if (e.getId().equals(i)) return e.getName();
        }
        return "알 수 없는 종목";
    }

    private void save() {
        JsonArray a = new JsonArray();
        for (ExerciseMaster e : x) a.add(e.toJson());
        s.save(FILE, a);
    }
}
