package com.library.libraryService.audit.controller;

import com.library.libraryService.audit.dto.AuditLogResponseDto;
import com.library.libraryService.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/audit")
@Tag(name = "Audit", description = "Audit log for super admin")
@SecurityRequirement(name = "bearer-jwt")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Operation(summary = "view audit log sorted using bubble sort algorithm")
    @GetMapping("/logs/{sortType}")
    public ResponseEntity<List<AuditLogResponseDto>> getAllLogs(@PathVariable @NotNull(message = "sortType wajib diisi") @NotBlank(message = "sortType tidak boleh kosong") String sortType ) {
        return ResponseEntity.ok(auditLogService.getAllLogs(sortType));
    }
}
