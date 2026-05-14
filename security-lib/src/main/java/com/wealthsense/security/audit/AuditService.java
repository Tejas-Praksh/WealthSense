package com.wealthsense.security.audit;

import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void save(AuditLog auditLog) {
        auditRepository.save(auditLog);
    }
}

