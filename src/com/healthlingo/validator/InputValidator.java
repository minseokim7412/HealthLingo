package com.healthlingo.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputValidator {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int fails = 0;
    public int getFailCount() { return fails; }
    public String readDate(Scanner sc, String msg) {
        while (true) {
            com.healthlingo.ui.Ansi.prompt(msg);
            String raw = sc.nextLine().trim();
            try {
                LocalDate.parse(raw, FMT);
                return raw;
            } catch (DateTimeParseException e) {
                fails++;
                com.healthlingo.ui.Ansi.alert("[알림] 날짜 형식이 올바르지 않습니다. 예) 2026-09-13 (EH-01)");
            }
        }
    }
    public String readOptional(Scanner sc, String msg) {
        com.healthlingo.ui.Ansi.prompt(msg);
        return sc.nextLine().trim();
    }
    public String readNonEmpty(Scanner sc, String msg) {
        while (true) {
            com.healthlingo.ui.Ansi.prompt(msg);
            String raw = sc.nextLine().trim();
            if (!raw.isEmpty()) return raw;
            fails++;
            com.healthlingo.ui.Ansi.alert("[알림] 값을 입력해야 합니다. (EH-01)");
        }
    }
    public double readNonNegativeDouble(Scanner sc, String msg) {
        while (true) {
            com.healthlingo.ui.Ansi.prompt(msg);
            String raw = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(raw);
                if (v < 0) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                fails++;
                com.healthlingo.ui.Ansi.alert("[알림] 숫자 형식이 올바르지 않습니다. 0 이상의 숫자를 입력하세요. (EH-01)");
            }
        }
    }
    public double readPositiveDouble(Scanner sc, String msg) {
        while (true) {
            double v = readNonNegativeDouble(sc, msg);
            if (v > 0) return v;
            fails++;
            com.healthlingo.ui.Ansi.alert("[알림] 0보다 큰 값을 입력해야 합니다. (EH-01)");
        }
    }
    public int readPositiveInt(Scanner sc, String msg) {
        while (true) {
            com.healthlingo.ui.Ansi.prompt(msg);
            String raw = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(raw);
                if (v > 0) return v;
                throw new NumberFormatException();
            } catch (NumberFormatException e) {
                fails++;
                com.healthlingo.ui.Ansi.alert("[알림] 1 이상의 정수를 입력하세요. (EH-01)");
            }
        }
    }
    public int readMenuChoice(Scanner sc, String msg, int min, int max) {
        while (true) {
            com.healthlingo.ui.Ansi.prompt(msg);
            String raw = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(raw);
                if (v >= min && v <= max) return v;
                throw new NumberFormatException();
            } catch (NumberFormatException e) {
                fails++;
                com.healthlingo.ui.Ansi.alert("[알림] " + min + "~" + max + " 범위의 번호를 입력하세요. (EH-01)");
            }
        }
    }
}
