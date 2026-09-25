package com.healthlingo.manager;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.model.BodyRecord;
import com.healthlingo.model.UserProfile;
import com.healthlingo.storage.JsonStorage;
import com.healthlingo.validator.BodyProfileValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * MOD-003 BodyManager (FR-02, EH-05)
 * 신체 변화 기록 등록·수정 및 BMI 자동 계산을 담당한다.
 * UserProfile(user.json)도 신체 정보의 일부(신장)로서 함께 관리한다.
 */
public class BodyManager {

    private static final String BODY_FILE = "body.json";
    private static final String USER_FILE = "user.json";

    private final JsonStorage stg;
    private final BodyProfileValidator bpv;
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

    /** 최초 프로필 등록/신장 수정. height=0을 입력하면 미등록 상태로 남겨 EH-05 시나리오를 재현할 수 있다. */
    public UserProfile registerProfile(String name, double height, String joinDate) {
        UserProfile p = new UserProfile(name, height, joinDate);
        saveProfile(p);
        return p;
    }

    /**
     * 체중·체지방률·골격근량을 등록/수정한다. (설계서 6.3절: upsertBodyRecord(id?, weight, bodyFat, muscleMass))
     * id가 비어있으면 신규 등록, 기존 id가 주어지면 해당 레코드의 값을 갱신한다.
     * 신장이 등록되어 있으면 BMI를 계산하고, 없으면 EH-05에 따라 계산을 생략한다.
     * @return 등록/수정된 BodyRecord
     */
    public BodyRecord upsertBodyRecord(String id, String date, double weight, double bodyFat, double muscleMass) {
        double bmi = 0;
        if (bpv.isValidForBmi(prof)) {
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
