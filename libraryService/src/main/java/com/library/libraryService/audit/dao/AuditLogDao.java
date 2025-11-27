package com.library.libraryService.audit.dao;

import com.library.libraryService.article.entity.Article;
import com.library.libraryService.audit.entity.AuditLog;

import java.util.List;

public interface AuditLogDao {

    AuditLog save(AuditLog auditLog);
    List<AuditLog> findAll();
}
