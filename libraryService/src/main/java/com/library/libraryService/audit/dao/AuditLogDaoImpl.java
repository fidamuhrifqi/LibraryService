package com.library.libraryService.audit.dao;

import com.library.libraryService.audit.entity.AuditLog;
import com.library.libraryService.audit.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AuditLogDaoImpl implements AuditLogDao {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
}
