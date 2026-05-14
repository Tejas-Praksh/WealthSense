package com.wealthsense.security.masking;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class DataMaskingService {

    public String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String[] parts = email.split("@", 2);
        if (parts.length != 2) {
            return "***";
        }
        String local = parts[0];
        String domain = parts[1];

        String prefix = local.length() >= 2 ? local.substring(0, 2) : local;
        return prefix + "***@" + domain;
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "*".repeat(Math.max(0, digits.length())) + digits;
        }
        int maskedLen = digits.length() - 4;
        return "*".repeat(maskedLen) + digits.substring(digits.length() - 4);
    }

    public String maskAccountNumber(String number) {
        if (number == null || number.isBlank()) {
            return null;
        }
        String digits = number.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "*".repeat(Math.max(0, digits.length())) + digits;
        }
        int maskedLen = digits.length() - 4;
        return "*".repeat(maskedLen) + digits.substring(digits.length() - 4);
    }

    public String maskAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authorized = auth != null && auth.getAuthorities().stream()
                .filter(Objects::nonNull)
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        return authorized ? amount.toPlainString() : "****";
    }
}

