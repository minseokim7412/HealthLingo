package com.healthlingo.manager;

import com.healthlingo.model.BodyRecord;
import com.healthlingo.model.Goal;
import com.healthlingo.model.WorkoutRecord;
import com.healthlingo.validator.BodyProfileValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsManager {
    public enum Period { DAILY, WEEKLY, MONTHLY, ALL }
    private final WorkoutManager wm;
    private final BodyManager bm;
    private final NotificationManager nm;
    private final BodyProfileValidator bpv;
    public StatisticsManager(WorkoutManager wm, BodyManager bm, NotificationManager nm, BodyProfileValidator bpv) {
        this.wm = wm;
        this.bm = bm;
        this.nm = nm;
        this.bpv = bpv;
    }
    public static class StatResult {
        public String label;
        public int total;
        public final List<String> lines = new ArrayList<>();
        public boolean bOk;
        public String wLine;
        public String bmiLine;
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

        Map<String, Double> bestByExercise = new LinkedHashMap<>();
        for (WorkoutRecord r : wrs) {
            bestByExercise.merge(r.getExerciseId(), r.getMaxWeight(), Math::max);
        }
        Map<String, Goal> goalByExercise = new HashMap<>();
        for (Goal g : nm.getGoals()) {
            if (g.isActive(today)) goalByExercise.put(g.getType(), g);
        }
        for (Map.Entry<String, Double> e : bestByExercise.entrySet()) {
            String exerciseId = e.getKey();
            double best = e.getValue();
            String name = wm.getExerciseName(exerciseId);
            Goal goal = goalByExercise.get(exerciseId);
            if (goal != null && goal.getTargetValue() > 0) {
                double rate = Math.min(999, (best / goal.getTargetValue()) * 100);
                int fill = Math.min(10, (int) Math.round(rate / 10));
                String bar = repeat("#", fill) + repeat("-", 10 - fill);
                res.lines.add(name + " 최고 중량: " + trimNumber(best) + "kg  [" + bar + "] 목표 대비 " + Math.round(rate) + "%");
            } else {
                res.lines.add(name + " 최고 중량: " + trimNumber(best) + "kg (목표 미설정)");
            }
        }
        res.bOk = bpv.isValidForStats(bm.getProfile(), bm.getLatestRecord());
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
                res.bmiLine = (bm.isBmiCalculated(first) && bm.isBmiCalculated(last))
                        ? String.format("%.1f", first.getBmi()) + " -> " + String.format("%.1f", last.getBmi())
                        : "계산 생략(EH-05)";
            } else {
                res.wLine = "해당 기간 신체 기록 없음";
                res.bmiLine = "해당 기간 신체 기록 없음";
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
