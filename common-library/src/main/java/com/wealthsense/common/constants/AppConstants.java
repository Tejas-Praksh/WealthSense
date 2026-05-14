package com.wealthsense.common.constants;

public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Constants class — do not instantiate");
    }

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String DEFAULT_CURRENCY = "INR";
    public static final String INDIA_TIMEZONE = "Asia/Kolkata";

    /** 1 lakh rupees in paise */
    public static final long MAX_TRANSACTION_AMOUNT_PAISE = 10_000_000L;

    /** 1 rupee in paise */
    public static final long MIN_TRANSACTION_AMOUNT_PAISE = 100L;
}
