package com.medical.appointmentmanagement.validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

public final class InputValidation {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^(?:07[0-9]{8}|\\+947[0-9]{8})$");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private InputValidation() { }
    public static String email(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() > 254 || !EMAIL.matcher(cleaned).matches() || cleaned.contains("..")) {
            throw new IllegalArgumentException("Enter a valid email address (example: name@example.com).");
        }
        return cleaned;
    }
    public static String phone(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (!PHONE.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Enter a Sri Lankan mobile number: 0771234567 or +94771234567.");
        }
        return cleaned;
    }
    public static LocalDate date(String value) {
        if (value == null || !value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
            throw new IllegalArgumentException("Date must be YYYY-MM-DD.");
        try { return LocalDate.parse(value, DATE); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Enter a valid calendar date (YYYY-MM-DD)."); }
    }
    public static LocalTime time(String value) {
        if (value == null || !value.matches("[0-9]{2}:[0-9]{2}"))
            throw new IllegalArgumentException("Time must be 24-hour HH:MM.");
        try { return LocalTime.parse(value, TIME); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Enter a valid 24-hour time (HH:MM)."); }
    }
    public static void futureDateTime(String date, String time) {
        if (!LocalDateTime.of(date(date), time(time)).isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Choose a future date and time.");
    }
    public static void schedule(String date, String start, String end) {
        LocalDate d = date(date);
        LocalTime s = time(start);
        LocalTime e = time(end);
        if (!e.isAfter(s)) throw new IllegalArgumentException("End time must be after start time.");
        if (!LocalDateTime.of(d, s).isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Schedule start must be in the future.");
    }
}
