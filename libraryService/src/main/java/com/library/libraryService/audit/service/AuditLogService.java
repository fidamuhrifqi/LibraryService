package com.library.libraryService.audit.service;

import com.library.libraryService.audit.dto.AuditLogResponseDto;

import java.util.List;

public interface AuditLogService {
    void log(String action, String resourceType, String resourceId);
    void log(String action, String resourceType, String resourceId, String usernameOverride);
    List<AuditLogResponseDto> getAllLogs(String sortType);
}
