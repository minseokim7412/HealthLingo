package com.healthlingo.manager;

import com.healthlingo.json.JsonArray;
import com.healthlingo.json.JsonObject;
import com.healthlingo.model.BadgeMaster;
import com.healthlingo.model.BadgeRecord;
import com.healthlingo.model.PointsAccount;
import com.healthlingo.storage.JsonStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RewardManager {
    private static final String POINTS_FILE = "points.json";
    private static final String BADGE_FILE = "badge.json";
    private static final int[] MILESTONES = {100, 300, 500};
    private static final int MILESTONE_REWARD = 50;
    private static final int DRAW_COST = 100;
    private final JsonStorage stg;
    private final Random rnd = new Random();

    private PointsAccount pts;
    private final List<BadgeMaster> mstrs = new ArrayList<>();
    private final List<BadgeRecord> owned = new ArrayList<>();
    public RewardManager(JsonStorage stg) {
        this.stg = stg;
        load();
    }
    private void load() {
        JsonObject root = stg.loadObject(BADGE_FILE);
        JsonArray marr = root.getJsonArray("masters");
        if (marr.size() == 0) {
            initDefaultBadges();
        } else {
            for (Object item : marr) mstrs.add(BadgeMaster.fromJson((JsonObject) item));
            for (Object item : root.getJsonArray("records")) owned.add(BadgeRecord.fromJson((JsonObject) item));
        }
        JsonObject pobj = stg.loadObject(POINTS_FILE);
        pts = pobj.has("currentPoint") ? PointsAccount.fromJson(pobj) : new PointsAccount();
    }
    private void initDefaultBadges() {
        mstrs.add(new BadgeMaster("B01", "새싹 밴드", "일반", 0.30));
        mstrs.add(new BadgeMaster("B02", "불꽃 스니커즈", "희귀", 0.15));
        mstrs.add(new BadgeMaster("B03", "황금 덤벨", "영웅", 0.05));
        mstrs.add(new BadgeMaster("NONE", "꽝", "-", 0.50));
        saveBadges();
    }
    public void checkMilestones(String cat, int cnt) {
        for (int th : MILESTONES) {
            if (cnt < th) break;
            String key = cat + "_" + th;
            if (!pts.isMilestoneAwarded(key)) {
                pts.markMilestoneAwarded(key);
                pts.earn(MILESTONE_REWARD);
                com.healthlingo.ui.Ansi.done("[완료] " + cat + " 누적 " + th + "회 달성! " + MILESTONE_REWARD + "P가 지급되었습니다.");
            }
        }
        savePoints();
    }
    public int getCurrentPoint() {
        return pts.getCurrentPoint();
    }
    public static class DrawResult {
        public final boolean ok;
        public final boolean miss;
        public final String bName;
        public final String bGrd;
        DrawResult(boolean ok, boolean miss, String bName, String bGrd) {
            this.ok = ok;
            this.miss = miss;
            this.bName = bName;
            this.bGrd = bGrd;
        }
    }
    public DrawResult drawBadge() {
        if (pts.getCurrentPoint() < DRAW_COST) {
            return new DrawResult(false, false, null, null);
        }
        pts.use(DRAW_COST);
        savePoints();
        BadgeMaster drawn = pickByProbability();
        if (drawn.isMiss()) {
            return new DrawResult(true, true, null, null);
        }
        BadgeRecord rec = findRecord(drawn.getId());
        if (rec == null) {
            rec = new BadgeRecord(drawn.getId(), 0, LocalDate.now().toString());
            owned.add(rec);
        }
        rec.increment();
        saveBadges();
        return new DrawResult(true, false, drawn.getName(), drawn.getGrade());
    }
    private BadgeMaster pickByProbability() {
        double tw = 0;
        for (BadgeMaster m : mstrs) tw += m.getProbability();
        double r = rnd.nextDouble() * tw;
        double cum = 0;
        for (BadgeMaster m : mstrs) {
            cum += m.getProbability();
            if (r < cum) return m;
        }
        return mstrs.get(mstrs.size() - 1);
    }
    private BadgeRecord findRecord(String badgeId) {
        for (BadgeRecord r : owned) {
            if (r.getBadgeId().equals(badgeId)) return r;
        }
        return null;
    }
    public List<BadgeRecord> getOwnedBadges() {
        return owned;
    }
    public String getBadgeName(String badgeId) {
        for (BadgeMaster m : mstrs) {
            if (m.getId().equals(badgeId)) return m.getName();
        }
        return "알 수 없는 뱃지";
    }
    public String getBadgeGrade(String badgeId) {
        for (BadgeMaster m : mstrs) {
            if (m.getId().equals(badgeId)) return m.getGrade();
        }
        return "-";
    }
    private void savePoints() {
        stg.save(POINTS_FILE, pts.toJson());
    }
    private void saveBadges() {
        JsonArray marr = new JsonArray();
        for (BadgeMaster m : mstrs) marr.add(m.toJson());
        JsonArray rarr = new JsonArray();
        for (BadgeRecord r : owned) rarr.add(r.toJson());
        JsonObject root = new JsonObject().put("masters", marr).put("records", rarr);
        stg.save(BADGE_FILE, root);
    }
}
