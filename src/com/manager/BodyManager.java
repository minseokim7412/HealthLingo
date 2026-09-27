// s=JsonStorage, b=BodyProfileValidator, v=InputValidator, l=신체기록 목록(recs), p=사용자 프로필(prof), q=ID 발급 순번(seq)
// c=Scanner, i=기록ID, d=날짜, w=체중, f=체지방률, m=골격근량, k=BMI 계산값, x=리스트 인덱스
// j=신규 ID 문자열/로드한 JSON객체, e=기존기록/예외, r=BodyRecord 임시값, a=JsonArray 임시값, n=파싱 카운터
package com.manager;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;
import com.model.BodyModels.BodyRecord;
import com.model.BodyModels.UserProfile;
import com.storage.JsonStorage;
import com.validator.BodyProfileValidator;
import com.validator.InputValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BodyManager {

    private static final String BODY_FILE = "body.json";
    private static final String USER_FILE = "user.json";

    private final JsonStorage s;
    private final BodyProfileValidator b;
    private final InputValidator v = new InputValidator();
    private final List<BodyRecord> l = new ArrayList<>();
    private UserProfile p;
    private int q = 1;

    public BodyManager(JsonStorage s, BodyProfileValidator b) {
        this.s = s;
        this.b = b;
        load();
    }

    private void load() {
        JsonObject j = s.loadObject(USER_FILE);
        p = j.has("name") ? UserProfile.fromJson(j) : null;
        for (Object i : s.loadArray(BODY_FILE)) {
            BodyRecord r = BodyRecord.fromJson((JsonObject) i);
            l.add(r);
            int n;
            try {
                n = Integer.parseInt(r.getId().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                n = 0;
            }
            if (n >= q) q = n + 1;
        }
    }

    public UserProfile getProfile() {
        return p;
    }

    public void saveProfile(UserProfile p) {
        this.p = p;
        s.save(USER_FILE, p.toJson());
    }

    public UserProfile registerProfile(String n, double h, String j) {
        UserProfile p = new UserProfile(n, h, j);
        saveProfile(p);
        return p;
    }

    public UserProfile registerProfileInteractive(Scanner c) {
        String n = v.readNonEmpty(c, "이름을 입력하세요 >> ");
        System.out.println("신장(m) 입력 >> 나중에 입력하려면 0을 입력하세요.");
        double h = v.readNonNegativeDouble(c, "신장(m) >> ");
        return registerProfile(n, h, LocalDate.now().toString());
    }

    public BodyRecord upsertBodyRecord(String i, String d, double w, double f, double m) {
        double k = 0;
        if (b.isValidForBmi(p, w)) {
            k = w / (p.getHeight() * p.getHeight());
        }
        if (i != null && !i.isEmpty()) {
            int x = indexOfId(i);
            if (x >= 0) {
                BodyRecord r = new BodyRecord(i, d, w, f, m, k);
                l.set(x, r);
                save();
                return r;
            }
        }
        String j = "B" + String.format("%04d", q++);
        BodyRecord r = new BodyRecord(j, d, w, f, m, k);
        l.add(r);
        save();
        return r;
    }

    public BodyRecord upsertBodyRecordInteractive(Scanner c, String i) {
        BodyRecord e = (i == null || i.isEmpty()) ? null : findById(i);
        String d = (e != null) ? e.getDate() : LocalDate.now().toString();
        double w = v.readPositiveDouble(c, "체중(kg) 입력 >> ");
        double f = v.readNonNegativeDouble(c, "체지방률(%) 입력 >> ");
        double m = v.readNonNegativeDouble(c, "골격근량(kg) 입력 >> ");
        return upsertBodyRecord(i, d, w, f, m);
    }

    public BodyRecord findById(String i) {
        int x = indexOfId(i);
        return x >= 0 ? l.get(x) : null;
    }

    private int indexOfId(String i) {
        for (int x = 0; x < l.size(); x++) {
            if (l.get(x).getId().equals(i)) return x;
        }
        return -1;
    }

    public boolean isBmiCalculated(BodyRecord r) {
        return r.getBmi() > 0;
    }

    public List<BodyRecord> getAllRecords() {
        return l;
    }

    public BodyRecord getLatestRecord() {
        return l.isEmpty() ? null : l.get(l.size() - 1);
    }

    private void save() {
        JsonArray a = new JsonArray();
        for (BodyRecord r : l) a.add(r.toJson());
        s.save(BODY_FILE, a);
    }
}
