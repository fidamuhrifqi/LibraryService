package com.library.libraryService.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
