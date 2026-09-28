package com.gymlogfx.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DateUtil - Utility class for date formatting operations.
 */
public class DateUtil {
    private static final DateTimeFormatter DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DB_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String toDisplayString(LocalDate date) {
        return date != null ? date.format(DISPLAY_FORMAT) : "";
    }

    public static String toDbString(LocalDate date) {
        return date != null ? date.format(DB_FORMAT) : "";
    }

    public static LocalDate fromDbString(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DB_FORMAT);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    public static String today() {
        return LocalDate.now().format(DB_FORMAT);
    }
}
