package com.wealthsense.common.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyUtilTest {

    @Test
    void paiseToRupees_hundredPaise_returnsOneRupee() {
        BigDecimal result = MoneyUtil.paiseToRupees(BigDecimal.valueOf(100));
        assertEquals(0, result.compareTo(BigDecimal.ONE));
    }

    @Test
    void rupeesToPaise_oneRupee_returnsHundredPaise() {
        BigDecimal result = MoneyUtil.rupeesToPaise(BigDecimal.ONE);
        assertEquals(0, result.compareTo(BigDecimal.valueOf(100)));
    }

    @Test
    void paiseToRupees_fractionalResult_scaleIsTwo() {
        BigDecimal result = MoneyUtil.paiseToRupees(BigDecimal.valueOf(1050));
        assertEquals(2, result.scale());
        assertEquals(0, result.compareTo(new BigDecimal("10.50")));
    }

    @Test
    void formatAmount_thousandRupees_returnsFormattedString() {
        // 100000 paise = 1000 rupees
        String result = MoneyUtil.formatAmount(BigDecimal.valueOf(100000));
        assertTrue(result.contains("1,000"), "Expected formatted amount to contain '1,000' but was: " + result);
    }

    @Test
    void formatAmount_singleRupee_returnsFormattedString() {
        String result = MoneyUtil.formatAmount(BigDecimal.valueOf(100));
        assertTrue(result.contains("1"), "Expected formatted amount to contain '1' but was: " + result);
    }

    @Test
    void isValidAmount_negativeAmount_returnsFalse() {
        assertFalse(MoneyUtil.isValidAmount(BigDecimal.valueOf(-100)));
    }

    @Test
    void isValidAmount_validAmount_returnsTrue() {
        assertTrue(MoneyUtil.isValidAmount(BigDecimal.valueOf(1000)));
    }

    @Test
    void isValidAmount_null_returnsFalse() {
        assertFalse(MoneyUtil.isValidAmount(null));
    }

    @Test
    void isValidAmount_zero_returnsFalse() {
        assertFalse(MoneyUtil.isValidAmount(BigDecimal.ZERO));
    }

    @Test
    void isValidAmount_exceedsMax_returnsFalse() {
        assertFalse(MoneyUtil.isValidAmount(BigDecimal.valueOf(20_000_000)));
    }

    @Test
    void isValidAmount_belowMin_returnsFalse() {
        assertFalse(MoneyUtil.isValidAmount(BigDecimal.valueOf(50)));
    }

    @Test
    void addAmounts_twoAmounts_returnsCorrectSum() {
        BigDecimal result = MoneyUtil.addAmounts(
                BigDecimal.valueOf(100), BigDecimal.valueOf(200));
        assertEquals(0, result.compareTo(BigDecimal.valueOf(300)));
    }

    @Test
    void subtractAmounts_twoAmounts_returnsCorrectDifference() {
        BigDecimal result = MoneyUtil.subtractAmounts(
                BigDecimal.valueOf(500), BigDecimal.valueOf(200));
        assertEquals(0, result.compareTo(BigDecimal.valueOf(300)));
    }
}
