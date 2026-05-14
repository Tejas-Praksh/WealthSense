package com.wealthsense.common.util;

import com.wealthsense.common.constants.AppConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtil {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final Locale INDIA_LOCALE = Locale.of("en", "IN");

    private MoneyUtil() {
        throw new UnsupportedOperationException("Utility class — do not instantiate");
    }

    /**
     * Convert paise to rupees with 2 decimal precision.
     */
    public static BigDecimal paiseToRupees(BigDecimal paise) {
        return paise.divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * Convert rupees to paise (whole number).
     */
    public static BigDecimal rupeesToPaise(BigDecimal rupees) {
        return rupees.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Format paise amount as Indian rupee display string (₹1,234.56).
     */
    public static String formatAmount(BigDecimal paise) {
        BigDecimal rupees = paiseToRupees(paise);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(rupees);
    }

    /**
     * Validate that a paise amount is within business rules:
     * not null, positive, within min/max transaction limits.
     */
    public static boolean isValidAmount(BigDecimal paise) {
        if (paise == null) {
            return false;
        }
        if (paise.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (paise.compareTo(BigDecimal.valueOf(AppConstants.MAX_TRANSACTION_AMOUNT_PAISE)) > 0) {
            return false;
        }
        return paise.compareTo(BigDecimal.valueOf(AppConstants.MIN_TRANSACTION_AMOUNT_PAISE)) >= 0;
    }

    public static BigDecimal addAmounts(BigDecimal a, BigDecimal b) {
        return a.add(b);
    }

    public static BigDecimal subtractAmounts(BigDecimal a, BigDecimal b) {
        return a.subtract(b);
    }
}
