// f=연속 입력 실패 횟수(fails), s=Scanner, m=프롬프트 메시지(msg), r=원시 입력 문자열(raw)
// v=파싱된 값(value), n=허용범위 최소값(min), x=허용범위 최대값(max), e=예외
package com.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputValidator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int f = 0;

    public int getFailCount() { return f; }

    public String readDate(Scanner s, String m) {
        while (true) {
            com.ConsoleUI.prompt(m);
            String r = s.nextLine().trim();
            try {
                LocalDate.parse(r, FMT);
                return r;
            } catch (DateTimeParseException e) {
                f++;
                com.ConsoleUI.alert("[알림] 날짜 형식이 올바르지 않습니다. 예) 2026-09-13 (EH-01)");
            }
        }
    }

    public String readOptional(Scanner s, String m) {
        com.ConsoleUI.prompt(m);
        return s.nextLine().trim();
    }

    public String readNonEmpty(Scanner s, String m) {
        while (true) {
            com.ConsoleUI.prompt(m);
            String r = s.nextLine().trim();
            if (!r.isEmpty()) return r;
            f++;
            com.ConsoleUI.alert("[알림] 값을 입력해야 합니다. (EH-01)");
        }
    }

    public double readNonNegativeDouble(Scanner s, String m) {
        while (true) {
            com.ConsoleUI.prompt(m);
            String r = s.nextLine().trim();
            try {
                double v = Double.parseDouble(r);
                if (v < 0) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                f++;
                com.ConsoleUI.alert("[알림] 숫자 형식이 올바르지 않습니다. 0 이상의 숫자를 입력하세요. (EH-01)");
            }
        }
    }

    public double readPositiveDouble(Scanner s, String m) {
        while (true) {
            double v = readNonNegativeDouble(s, m);
            if (v > 0) return v;
            f++;
            com.ConsoleUI.alert("[알림] 0보다 큰 값을 입력해야 합니다. (EH-01)");
        }
    }

    public int readPositiveInt(Scanner s, String m) {
        while (true) {
            com.ConsoleUI.prompt(m);
            String r = s.nextLine().trim();
            try {
                int v = Integer.parseInt(r);
                if (v > 0) return v;
                throw new NumberFormatException();
            } catch (NumberFormatException e) {
                f++;
                com.ConsoleUI.alert("[알림] 1 이상의 정수를 입력하세요. (EH-01)");
            }
        }
    }

    public int readMenuChoice(Scanner s, String m, int n, int x) {
        while (true) {
            com.ConsoleUI.prompt(m);
            String r = s.nextLine().trim();
            try {
                int v = Integer.parseInt(r);
                if (v >= n && v <= x) return v;
                throw new NumberFormatException();
            } catch (NumberFormatException e) {
                f++;
                com.ConsoleUI.alert("[알림] " + n + "~" + x + " 범위의 번호를 입력하세요. (EH-01)");
            }
        }
    }
}
