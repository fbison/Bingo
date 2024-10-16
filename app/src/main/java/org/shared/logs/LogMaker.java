package org.shared.logs;

public class LogMaker {

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    // Método para logar avisos
    public static void warn(String message) {
        System.out.println("[WARN] " + message);
    }

    // Método para logar erros
    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }
}
