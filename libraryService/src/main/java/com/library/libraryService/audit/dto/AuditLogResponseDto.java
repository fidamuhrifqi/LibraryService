package com.library.libraryService.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogResponseDto {

    private String id;
    private String username;
    private String action;
    private String resourceType;
    private String resourceId;
    private String method;
    private String path;
    private String userAgent;
    private String ipAddress;
    private Date timestamp;
}