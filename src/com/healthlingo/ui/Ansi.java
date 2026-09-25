package com.healthlingo.ui;

public final class Ansi {

    private static final String RESET = "\u001B[0m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";

    private Ansi() {
    }

    public static void alert(String msg) {
        System.err.println(YELLOW + msg + RESET);
    }

    public static void done(String msg) {
        System.out.println(GREEN + msg + RESET);
    }

    public static void prompt(String msg) {
        System.out.print(CYAN + msg + RESET);
    }
}

