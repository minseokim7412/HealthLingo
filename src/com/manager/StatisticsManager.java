// w=WorkoutManager, b=BodyManager, n=NotificationManager, p=BodyProfileValidator
// StatResult 내부: l=기간라벨(label), t=총운동횟수(total), i=종목별줄목록(lines), o=신체정보유효여부(bOk), w=체중변화줄(wLine), m=BMI변화줄(bmiLine)
// getStatistics: a=조회기간(period), r=결과(StatResult), y=오늘, f=기간시작일(from), s=기간내운동기록목록
// g=종목별최고기록Map, h=종목별목표Map, q=기간내신체기록목록, k=순회임시변수, d=파싱된날짜, e=Map.Entry
// x=종목ID/BodyRecord, v=최고기록값, z=종목명/BodyRecord, u=달성률, c=막대칸수, j=막대그래프문자열
package com.manager;

import com.model.BodyModels.BodyRecord;
import com.model.NotificationModels.Goal;
import com.model.WorkoutModels.WorkoutRecord;
import com.validator.BodyProfileValidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatisticsManager {

    public enum Period { DAILY, WEEKLY, MONTHLY, ALL }

    private final WorkoutManager w;
    private final BodyManager b;
    private final NotificationManager n;
    private final BodyProfileValidator p;

    public StatisticsManager(WorkoutManager w, BodyManager b, NotificationManager n, BodyProfileValidator p) {
        this.w = w;
        this.b = b;
        this.n = n;
        this.p = p;
    }

    public static class StatResult {
        public String l;
        public int t;
        public final List<String> i = new ArrayList<>();
        public boolean o;
        public String w;
        public String m;
    }

    public StatResult getStatistics(Period a) {
        StatResult r = new StatResult();
        LocalDate y = LocalDate.now();
        LocalDate f;
        switch (a) {
            case DAILY: f = y; r.l = "오늘"; break;
            case WEEKLY: f = y.minusDays(6); r.l = "최근 7일"; break;
            case MONTHLY: f = y.withDayOfMonth(1); r.l = "이번 달"; break;
            default: f = LocalDate.MIN; r.l = "전체 기간"; break;
        }

        List<WorkoutRecord> s = new ArrayList<>();
        for (WorkoutRecord k : w.getAllWorkouts()) {
            LocalDate d = LocalDate.parse(k.getDate());
            if (!d.isBefore(f) && !d.isAfter(y)) s.add(k);
        }
        r.t = s.size();

        Map<String, Double> g = new LinkedHashMap<>();
        for (WorkoutRecord k : s) {
            g.merge(k.getExerciseId(), k.getMaxWeight(), Math::max);
        }
        Map<String, Goal> h = new HashMap<>();
        for (Goal k : n.getGoals()) {
            if (k.isActive(y)) h.put(k.getType(), k);
        }

        for (Map.Entry<String, Double> e : g.entrySet()) {
            String x = e.getKey();
            double v = e.getValue();
            String z = w.getExerciseName(x);
            Goal k = h.get(x);
            if (k != null && k.getTargetValue() > 0) {
                double u = Math.min(999, (v / k.getTargetValue()) * 100);
                int c = Math.min(10, (int) Math.round(u / 10));
                String j = repeat("#", c) + repeat("-", 10 - c);
                r.i.add(z + " 최고 중량: " + trimNumber(v) + "kg  [" + j + "] 목표 대비 " + Math.round(u) + "%");
            } else {
                r.i.add(z + " 최고 중량: " + trimNumber(v) + "kg (목표 미설정)");
            }
        }

        r.o = p.isValidForStats(b.getProfile(), b.getLatestRecord());
        if (r.o) {
            List<BodyRecord> q = new ArrayList<>();
            for (BodyRecord k : b.getAllRecords()) {
                LocalDate d = LocalDate.parse(k.getDate());
                if (!d.isBefore(f) && !d.isAfter(y)) q.add(k);
            }
            if (!q.isEmpty()) {
                BodyRecord x = q.get(0);
                BodyRecord z = q.get(q.size() - 1);
                r.w = trimNumber(x.getWeight()) + "kg -> " + trimNumber(z.getWeight()) + "kg";
                r.m = (b.isBmiCalculated(x) && b.isBmiCalculated(z))
                        ? String.format("%.1f", x.getBmi()) + " -> " + String.format("%.1f", z.getBmi())
                        : "계산 생략(EH-05)";
            } else {
                r.w = "해당 기간 신체 기록 없음";
                r.m = "해당 기간 신체 기록 없음";
            }
        }

        return r;
    }

    private String repeat(String s, int c) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < c; i++) b.append(s);
        return b.toString();
    }

    private String trimNumber(double v) {
        if (v == Math.floor(v)) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}
