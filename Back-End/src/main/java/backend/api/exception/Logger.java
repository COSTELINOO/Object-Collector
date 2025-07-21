package backend.api.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Logger {

    // Coduri ANSI pentru culorile din consola
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[91m";       // ERROR - Rosu aprins
    private static final String YELLOW = "\u001B[33m";    // WARNING - Galben
    private static final String WHITE = "\u001B[97m";     // INFO - Alb / Gri
    private static final String GREEN = "\u001B[32m";     // REQUEST - Verde
    private static final String CYAN = "\u001B[36m";      // DEBUG - Cyan / Albastru
    private static final String LIGHT_GREEN = "\u001B[92m"; // SUCCESS - Verde deschis
    private static final String PURPLE = "\u001B[35m";    // EXCEPTION - Violet
    private static final String MAGENTA = "\u001B[95m";   // MALICIOUS - Magenta deschis

    // Format pentru timestamp
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public enum LogLevel {
        ERROR, WARNING, INFO, REQUEST, DEBUG, SUCCESS, EXCEPTION, MALICIOUS
    }

    public static void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String prefix;
        String color = switch (level) {
            case ERROR -> {
                prefix = "[ERROR]";
                yield RED;
            }
            case WARNING -> {
                prefix = "[WARNING]";
                yield YELLOW;
            }
            case REQUEST -> {
                prefix = "[REQUEST]";
                yield GREEN;
            }
            case DEBUG -> {
                prefix = "[DEBUG]";
                yield CYAN;
            }
            case SUCCESS -> {
                prefix = "[SUCCESS]";
                yield LIGHT_GREEN;
            }
            case EXCEPTION -> {
                prefix = "[EXCEPTION]";
                yield PURPLE;
            }
            case MALICIOUS -> {
                prefix = "[MALICIOUS]";
                yield MAGENTA;
            }
            default -> {
                prefix = "[INFO]";
                yield WHITE;
            }
        };

        String consoleMessage = color + timestamp + " " + prefix + " " + message + RESET;
        System.out.println(consoleMessage);

    }

    public static void error(String msg) {
        log(LogLevel.ERROR, msg);
    }

    public static void warning(String msg) {
        log(LogLevel.WARNING, msg);
    }

    public static void info(String msg) {
        log(LogLevel.INFO, msg);
    }

    public static void request(String msg) {
        log(LogLevel.REQUEST, msg);
    }

    public static void debug(String msg) {
        log(LogLevel.DEBUG, msg);
    }

    public static void success(String msg) {
        log(LogLevel.SUCCESS, msg);
    }

    public static void exception(String msg) {
        log(LogLevel.EXCEPTION, msg);
    }

    public static void exception(Exception e) {
        exception(e.getClass().getSimpleName());
        error(e.getMessage());
    }

    public static void malicious(String msg) {
        log(LogLevel.MALICIOUS, msg);
    }
}