package com.healthlingo.manager;

import com.healthlingo.model.BodyRecord;
import com.healthlingo.model.Goal;
import com.healthlingo.model.WorkoutRecord;
import com.healthlingo.validator.BodyProfileValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MOD-006 StatisticsManager (FR-05, EH-03)
 * 기간별(일간/주간/월간/전체) 통계·그래프·달성률을 산출한다.
 */
public class StatisticsManager {

    public enum Period { DAILY, WEEKLY, MONTHLY, ALL }

    private final WorkoutManager wm;
    private final BodyManager bm;
    private final ExerciseCatalogManager cat;
    private final NotificationManager nm;
    private final BodyProfileValidator bpv;

    public StatisticsManager(WorkoutManager wm, BodyManager bm, ExerciseCatalogManager cat,
                              NotificationManager nm, BodyProfileValidator bpv) {
        this.wm = wm;
        this.bm = bm;
        this.cat = cat;
        this.nm = nm;
        this.bpv = bpv;
    }

    public static class StatResult {
        public String label;
        public int total;
        public final List<String> lines = new ArrayList<>();
        public boolean bOk;
        public String wLine;
    }

    public StatResult getStatistics(Period period) {
        StatResult res = new StatResult();
        LocalDate today = LocalDate.now();
        LocalDate from;
        switch (period) {
            case DAILY: from = today; res.label = "오늘"; break;
            case WEEKLY: from = today.minusDays(6); res.label = "최근 7일"; break;
            case MONTHLY: from = today.withDayOfMonth(1); res.label = "이번 달"; break;
            default: from = LocalDate.MIN; res.label = "전체 기간"; break;
        }

        List<WorkoutRecord> wrs = new ArrayList<>();
        for (WorkoutRecord r : wm.getAllWorkouts()) {
            LocalDate d = LocalDate.parse(r.getDate());
            if (!d.isBefore(from) && !d.isAfter(today)) wrs.add(r);
        }
        res.total = wrs.size();

        for (Goal goal : nm.getGoals()) {
            String exerciseId = cat.findIdByName(goal.getType());
            double best = (exerciseId == null) ? 0 : wm.getMaxWeightForExercise(wrs, exerciseId);
            double rate = goal.getTargetValue() <= 0 ? 0 : Math.min(999, (best / goal.getTargetValue()) * 100);
            int fill = Math.min(10, (int) Math.round(rate / 10));
            String bar = repeat("#", fill) + repeat("-", 10 - fill);
            res.lines.add(goal.getType() + " 최고 기록: " + trimNumber(best)
                    + "  [" + bar + "] 목표 대비 " + Math.round(rate) + "%");
        }

        // EH-03: 신체 정보(신장) 미등록 시 체중·BMI 관련 통계는 생략한다.
        res.bOk = bpv.isValidForStats(bm.getProfile());
        if (res.bOk) {
            List<BodyRecord> brs = new ArrayList<>();
            for (BodyRecord r : bm.getAllRecords()) {
                LocalDate d = LocalDate.parse(r.getDate());
                if (!d.isBefore(from) && !d.isAfter(today)) brs.add(r);
            }
            if (!brs.isEmpty()) {
                BodyRecord first = brs.get(0);
                BodyRecord last = brs.get(brs.size() - 1);
                res.wLine = trimNumber(first.getWeight()) + "kg -> " + trimNumber(last.getWeight()) + "kg";
            } else {
                res.wLine = "해당 기간 신체 기록 없음";
            }
        }

        return res;
    }

    private String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append(s);
        return sb.toString();
    }

    private String trimNumber(double value) {
        if (value == Math.floor(value)) return String.valueOf((long) value);
        return String.valueOf(value);
    }
}
