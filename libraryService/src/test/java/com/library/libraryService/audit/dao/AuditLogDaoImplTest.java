package com.library.libraryService.audit.dao;

import com.library.libraryService.audit.entity.AuditLog;
import com.library.libraryService.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class AuditLogDaoImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogDaoImpl auditLogDao;

    @Test
    @DisplayName("findAll() sukses get dao findAll")
    void findAll_shouldReturnListFromRepository() {
        AuditLog a1 = new AuditLog();
        AuditLog a2 = new AuditLog();
        List<AuditLog> list = List.of(a1, a2);

        Mockito.when(auditLogRepository.findAll()).thenReturn(list);

        List<AuditLog> result = auditLogDao.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(a1, a2);
        Mockito.verify(auditLogRepository).findAll();
    }

    @Test
    @DisplayName("save() sukses get dao save")
    void save_shouldCallRepositorySave() {
        AuditLog auditLog = new AuditLog();
        Mockito.when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);

        AuditLog result = auditLogDao.save(auditLog);

        assertThat(result).isSameAs(auditLog);
        Mockito.verify(auditLogRepository).save(eq(auditLog));
    }
}
