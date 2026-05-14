package com.wealthsense.common.util;

import com.wealthsense.common.constants.AppConstants;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class DateTimeUtil {

    private static final ZoneId IST_ZONE = ZoneId.of(AppConstants.INDIA_TIMEZONE);
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a z", Locale.ENGLISH);

    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class — do not instantiate");
    }

    public static Instant nowUtc() {
        return Instant.now();
    }

    public static ZonedDateTime toIst(Instant instant) {
        return instant.atZone(IST_ZONE);
    }

    /**
     * Format as "23 Apr 2026, 2:30 PM IST".
     */
    public static String formatForDisplay(Instant instant) {
        return toIst(instant).format(DISPLAY_FORMATTER);
    }

    public static boolean isToday(Instant instant) {
        LocalDate today = LocalDate.now(IST_ZONE);
        LocalDate target = instant.atZone(IST_ZONE).toLocalDate();
        return today.equals(target);
    }

    public static long daysBetween(Instant from, Instant to) {
        return ChronoUnit.DAYS.between(from, to);
    }
}
