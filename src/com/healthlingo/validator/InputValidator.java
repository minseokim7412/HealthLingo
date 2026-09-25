package com.healthlingo.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * MOD-007 InputValidator (EH-01)
 * Scanner로 입력된 원시 문자열의 형식을 검증한다.
 * 형식 오류 시 프로그램을 종료하지 않고 같은 입력 단계에서 재입력을 요청한다.
 */
public class InputValidator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int fails = 0;

    public int getFailCount() { return fails; }

    /** 날짜(yyyy-MM-dd) 형식 검증 후 반환 */
    public String readDate(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            String raw = sc.nextLine().trim();
            try {
                LocalDate.parse(raw, FMT);
                return raw;
            } catch (DateTimeParseException e) {
                fails++;
                System.out.println("[알림] 날짜 형식이 올바르지 않습니다. 예) 2026-09-13 (EH-01)");
            }
        }
    }

    /** 공백이 아닌 문자열 검증 */
    public String readNonEmpty(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            String raw = sc.nextLine().trim();
            if (!raw.isEmpty()) return raw;
            fails++;
            System.out.println("[알림] 값을 입력해야 합니다. (EH-01)");
        }
    }

    /** 0 이상의 실수 값 검증 (0은 '미입력/건너뛰기' 의미로 허용) */
    public double readNonNegativeDouble(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            String raw = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(raw);
                if (v < 0) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                fails++;
                System.out.println("[알림] 숫자 형식이 올바르지 않습니다. 0 이상의 숫자를 입력하세요. (EH-01)");
            }
        }
    }

    /** 0보다 큰 실수 값 검증 (무게 등) */
    public double readPositiveDouble(Scanner sc, String msg) {
        while (true) {
            double v = readNonNegativeDouble(sc, msg);
            if (v > 0) return v;
            fails++;
            System.out.println("[알림] 0보다 큰 값을 입력해야 합니다. (EH-01)");
        }
    }

    /** 0보다 큰 정수 값 검증 (횟수, 세트 수 등) */
    public int readPositiveInt(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            String raw = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(raw);
                if (v > 0) return v;
                throw new NumberFormatException();
            } catch (NumberFormatException e) {
                fails++;
                System.out.println("[알림] 1 이상의 정수를 입력하세요. (EH-01)");
            }
        }
    }

    /** min~max 범위의 메뉴 번호 검증 */
    public int readMenuChoice(Scanner sc, String msg, int min, int max) {
        while (true) {
            System.out.print(msg);
            String raw = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(raw);
                if (v >= min && v <= max) return v;
                throw new NumberFormatException();
            } catch (NumberFormatException e) {
                fails++;
                System.out.println("[알림] " + min + "~" + max + " 범위의 번호를 입력하세요. (EH-01)");
            }
        }
    }
}
