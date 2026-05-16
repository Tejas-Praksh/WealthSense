package com.wealthsense.security.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(AuditRepository.class)
public class AuditService {

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void save(AuditLog auditLog) {
        auditRepository.save(auditLog);
    }
}

