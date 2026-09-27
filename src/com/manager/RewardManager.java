// s=JsonStorage, r=Random, p=포인트계정(pts), m=뱃지마스터목록(mstrs), o=보유뱃지목록(owned)
// j=JSON객체임시변수, a/b=JsonArray임시변수, i=순회변수, k=임시결과(BadgeMaster/BadgeRecord 등)
// c=카테고리(cat)/누적확률, n=카운트(cnt), t=임계값(th)/확률합계, x=난수/최고기록/인덱스
// DrawResult 내부: o=성공여부, m=꽝여부, n=뱃지이름, g=뱃지등급
package com.manager;

import com.json.Json.JsonArray;
import com.json.Json.JsonObject;
import com.model.RewardModels.BadgeMaster;
import com.model.RewardModels.BadgeRecord;
import com.model.RewardModels.PointsAccount;
import com.storage.JsonStorage;

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

    private final JsonStorage s;
    private final Random r = new Random();

    private PointsAccount p;
    private final List<BadgeMaster> m = new ArrayList<>();
    private final List<BadgeRecord> o = new ArrayList<>();

    public RewardManager(JsonStorage s) {
        this.s = s;
        load();
    }

    private void load() {
        JsonObject j = s.loadObject(BADGE_FILE);
        JsonArray a = j.getJsonArray("masters");
        if (a.size() == 0) {
            initDefaultBadges();
        } else {
            for (Object i : a) m.add(BadgeMaster.fromJson((JsonObject) i));
            for (Object i : j.getJsonArray("records")) o.add(BadgeRecord.fromJson((JsonObject) i));
        }
        JsonObject k = s.loadObject(POINTS_FILE);
        p = k.has("currentPoint") ? PointsAccount.fromJson(k) : new PointsAccount();
    }

    private void initDefaultBadges() {
        m.add(new BadgeMaster("B01", "새싹 밴드", "일반", 0.30));
        m.add(new BadgeMaster("B02", "불꽃 스니커즈", "희귀", 0.15));
        m.add(new BadgeMaster("B03", "황금 덤벨", "영웅", 0.05));
        m.add(new BadgeMaster("NONE", "꽝", "-", 0.50));
        saveBadges();
    }

    public void checkMilestones(String c, int n) {
        for (int t : MILESTONES) {
            if (n < t) break;
            String k = c + "_" + t;
            if (!p.isMilestoneAwarded(k)) {
                p.markMilestoneAwarded(k);
                p.earn(MILESTONE_REWARD);
                com.ConsoleUI.done("[완료] " + c + " 누적 " + t + "회 달성! " + MILESTONE_REWARD + "P가 지급되었습니다.");
            }
        }
        savePoints();
    }

    public int getCurrentPoint() {
        return p.getCurrentPoint();
    }

    public static class DrawResult {
        public final boolean o;
        public final boolean m;
        public final String n;
        public final String g;

        DrawResult(boolean o, boolean m, String n, String g) {
            this.o = o;
            this.m = m;
            this.n = n;
            this.g = g;
        }
    }

    public DrawResult drawBadge() {
        if (p.getCurrentPoint() < DRAW_COST) {
            return new DrawResult(false, false, null, null);
        }
        p.use(DRAW_COST);
        savePoints();

        BadgeMaster k = pickByProbability();
        if (k.isMiss()) {
            return new DrawResult(true, true, null, null);
        }

        BadgeRecord i = findRecord(k.getId());
        if (i == null) {
            i = new BadgeRecord(k.getId(), 0, LocalDate.now().toString());
            o.add(i);
        }
        i.increment();
        saveBadges();
        return new DrawResult(true, false, k.getName(), k.getGrade());
    }

    private BadgeMaster pickByProbability() {
        double t = 0;
        for (BadgeMaster i : m) t += i.getProbability();
        double x = r.nextDouble() * t;
        double c = 0;
        for (BadgeMaster i : m) {
            c += i.getProbability();
            if (x < c) return i;
        }
        return m.get(m.size() - 1);
    }

    private BadgeRecord findRecord(String i) {
        for (BadgeRecord k : o) {
            if (k.getBadgeId().equals(i)) return k;
        }
        return null;
    }

    public List<BadgeRecord> getOwnedBadges() {
        return o;
    }

    public String getBadgeName(String i) {
        for (BadgeMaster k : m) {
            if (k.getId().equals(i)) return k.getName();
        }
        return "알 수 없는 뱃지";
    }

    public String getBadgeGrade(String i) {
        for (BadgeMaster k : m) {
            if (k.getId().equals(i)) return k.getGrade();
        }
        return "-";
    }

    private void savePoints() {
        s.save(POINTS_FILE, p.toJson());
    }

    private void saveBadges() {
        JsonArray a = new JsonArray();
        for (BadgeMaster k : m) a.add(k.toJson());
        JsonArray b = new JsonArray();
        for (BadgeRecord k : o) b.add(k.toJson());
        JsonObject j = new JsonObject().put("masters", a).put("records", b);
        s.save(BADGE_FILE, j);
    }
}
