package com.healthlingo.manager;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.model.BodyRecord;
import com.healthlingo.model.UserProfile;
import com.healthlingo.storage.JsonStorage;
import com.healthlingo.validator.BodyProfileValidator;
import com.healthlingo.validator.InputValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BodyManager {
    private static final String BODY_FILE = "body.json";
    private static final String USER_FILE = "user.json";
    private final JsonStorage stg;
    private final BodyProfileValidator bpv;
    private final InputValidator val = new InputValidator();
    private final List<BodyRecord> recs = new ArrayList<>();
    private UserProfile prof;
    private int seq = 1;
    public BodyManager(JsonStorage stg, BodyProfileValidator bpv) {
        this.stg = stg;
        this.bpv = bpv;
        load();
    }

    private void load() {
        JsonObject uobj = stg.loadObject(USER_FILE);
        prof = uobj.has("name") ? UserProfile.fromJson(uobj) : null;
        for (Object item : stg.loadArray(BODY_FILE)) {
            BodyRecord r = BodyRecord.fromJson((JsonObject) item);
            recs.add(r);
            int n;
            try {
                n = Integer.parseInt(r.getId().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                n = 0;
            }
            if (n >= seq) seq = n + 1;
        }
    }
    public UserProfile getProfile() {
        return prof;
    }
    public void saveProfile(UserProfile p) {
        this.prof = p;
        stg.save(USER_FILE, p.toJson());
    }

    public UserProfile registerProfile(String name, double height, String joinDate) {
        UserProfile p = new UserProfile(name, height, joinDate);
        saveProfile(p);
        return p;
    }
    public UserProfile registerProfileInteractive(Scanner sc) {
        String name = val.readNonEmpty(sc, "이름을 입력하세요 >> ");
        System.out.println("신장(m) 입력 >> 나중에 입력하려면 0을 입력하세요.");
        double height = val.readNonNegativeDouble(sc, "신장(m) >> ");
        return registerProfile(name, height, LocalDate.now().toString());
    }
    public BodyRecord upsertBodyRecord(String id, String date, double weight, double bodyFat, double muscleMass) {
        double bmi = 0;
        if (bpv.isValidForBmi(prof, weight)) {
            bmi = weight / (prof.getHeight() * prof.getHeight());
        }
        if (id != null && !id.isEmpty()) {
            int idx = indexOfId(id);
            if (idx >= 0) {
                BodyRecord rec = new BodyRecord(id, date, weight, bodyFat, muscleMass, bmi);
                recs.set(idx, rec);
                save();
                return rec;
            }
        }
        String newId = "B" + String.format("%04d", seq++);
        BodyRecord rec = new BodyRecord(newId, date, weight, bodyFat, muscleMass, bmi);
        recs.add(rec);
        save();
        return rec;
    }
    public BodyRecord upsertBodyRecordInteractive(Scanner sc, String id) {
        BodyRecord existing = (id == null || id.isEmpty()) ? null : findById(id);
        String date = (existing != null) ? existing.getDate() : LocalDate.now().toString();
        double weight = val.readPositiveDouble(sc, "체중(kg) 입력 >> ");
        double bodyFat = val.readNonNegativeDouble(sc, "체지방률(%) 입력 >> ");
        double muscleMass = val.readNonNegativeDouble(sc, "골격근량(kg) 입력 >> ");
        return upsertBodyRecord(id, date, weight, bodyFat, muscleMass);
    }
    public BodyRecord findById(String id) {
        int idx = indexOfId(id);
        return idx >= 0 ? recs.get(idx) : null;
    }
    private int indexOfId(String id) {
        for (int i = 0; i < recs.size(); i++) {
            if (recs.get(i).getId().equals(id)) return i;
        }
        return -1;
    }
    public boolean isBmiCalculated(BodyRecord rec) {
        return rec.getBmi() > 0;
    }
    public List<BodyRecord> getAllRecords() {
        return recs;
    }
    public BodyRecord getLatestRecord() {
        return recs.isEmpty() ? null : recs.get(recs.size() - 1);
    }
    private void save() {
        JsonArray arr = new JsonArray();
        for (BodyRecord r : recs) arr.add(r.toJson());
        stg.save(BODY_FILE, arr);
    }
}
