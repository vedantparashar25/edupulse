package com.vityarthi.edupulse.util;

public final class AnsiUtil {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private AnsiUtil() {}

    public static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("===============================================================================");
        System.out.println("  ______ _____  _    _ _____  _    _ _       _____ ______ ");
        System.out.println(" |  ____|  __ \\| |  | |  __ \\| |  | | |     / ____|  ____|");
        System.out.println(" | |__  | |  | | |  | | |__) | |  | | |    | (___ | |__   ");
        System.out.println(" |  __| | |  | | |  | |  ___/| |  | | |     \\___ \\|  __|  ");
        System.out.println(" | |____| |__| | |__| | |    | |__| | |____ ____) | |____ ");
        System.out.println(" |______|_____/ \\____/|_|     \\____/|______|_____/|______|");
        System.out.println("       Smart Campus Academic & Performance Intelligence System");
        System.out.println("                 VITyarthi Flipped Course Platform");
        System.out.println("===============================================================================" + RESET);
    }

    public static void printHeader(String title) {
        System.out.println("\n" + CYAN + BOLD + ">>> " + title + " <<<" + RESET);
        System.out.println(CYAN + "--------------------------------------------------------" + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + BOLD + "[SUCCESS] " + RESET + GREEN + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + BOLD + "[ERROR] " + RESET + RED + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + BOLD + "[WARNING] " + RESET + YELLOW + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(BLUE + BOLD + "[INFO] " + RESET + message);
    }
}
