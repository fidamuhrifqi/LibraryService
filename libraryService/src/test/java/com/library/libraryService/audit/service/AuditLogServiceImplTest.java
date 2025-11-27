package com.library.libraryService.audit.service;

import com.library.libraryService.audit.dao.AuditLogDao;
import com.library.libraryService.audit.dto.AuditLogResponseDto;
import com.library.libraryService.audit.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogDao auditLogDao;

    @Spy
    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    @DisplayName("log harus ambil username dari SecurityContext dan info request dari RequestContextHolder")
    void log_shouldUseSecurityContextAndRequest() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("fida");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        try (MockedStatic<SecurityContextHolder> mockedSecurity =
                     Mockito.mockStatic(SecurityContextHolder.class);
             MockedStatic<RequestContextHolder> mockedRequest =
                     Mockito.mockStatic(RequestContextHolder.class)) {

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getMethod()).thenReturn("POST");
            when(request.getRequestURI()).thenReturn("/user/test");
            when(request.getHeader("User-Agent")).thenReturn("JUnit");
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            ServletRequestAttributes attrs = new ServletRequestAttributes(request);
            mockedRequest.when(RequestContextHolder::getRequestAttributes).thenReturn(attrs);

            auditLogService.log("CREATE", "USER", "123");

            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogDao).save(logCaptor.capture());

            AuditLog saved = logCaptor.getValue();
            assertThat(saved.getUsername()).isEqualTo("fida");
            assertThat(saved.getAction()).isEqualTo("CREATE");
            assertThat(saved.getResourceType()).isEqualTo("USER");
            assertThat(saved.getResourceId()).isEqualTo("123");
            assertThat(saved.getMethod()).isEqualTo("POST");
            assertThat(saved.getPath()).isEqualTo("/user/test");
            assertThat(saved.getUserAgent()).isEqualTo("JUnit");
            assertThat(saved.getIpAddress()).isEqualTo("127.0.0.1");
            assertThat(saved.getTimestamp()).isNotNull();
        }
    }

    @Test
    @DisplayName("log dengan usernameOverride harus pakai usernameOverride meskipun ada SecurityContext")
    void log_shouldUseUsernameOverride() {
        SecurityContext securityContext = mock(SecurityContext.class);

        try (MockedStatic<SecurityContextHolder> mockedSecurity =
                     Mockito.mockStatic(SecurityContextHolder.class);
             MockedStatic<RequestContextHolder> mockedRequest =
                     Mockito.mockStatic(RequestContextHolder.class)) {

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            mockedRequest.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            auditLogService.log("DELETE", "ARTICLE", "123123", "system");

            ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogDao).save(logCaptor.capture());

            AuditLog saved = logCaptor.getValue();
            assertThat(saved.getUsername()).isEqualTo("system");
            assertThat(saved.getAction()).isEqualTo("DELETE");
            assertThat(saved.getResourceType()).isEqualTo("ARTICLE");
            assertThat(saved.getResourceId()).isEqualTo("123123");
        }
    }

    @Test
    @DisplayName("getAllLogs(asc) harus mengembalikan list yang sudah di-sort asc menggunakan bubble sort")
    void getAllLogs_sortedAsc() {
        Date older = new Date(System.currentTimeMillis() - 10_000);
        Date newer = new Date(System.currentTimeMillis());

        AuditLog log1 = new AuditLog();
        log1.setId("1");
        log1.setUsername("user1");
        log1.setAction("A1");
        log1.setResourceType("rt");
        log1.setResourceId("R1");
        log1.setTimestamp(older);

        AuditLog log2 = new AuditLog();
        log2.setId("2");
        log2.setUsername("user2");
        log2.setAction("A2");
        log2.setResourceType("rt");
        log2.setResourceId("R2");
        log2.setTimestamp(newer);

        Mockito.doNothing().when(auditLogService).log(anyString(), anyString(), anyString());
        when(auditLogDao.findAll()).thenReturn(Arrays.asList(log2, log1));

        try (MockedStatic<SecurityContextHolder> mockedSecurity =
                     Mockito.mockStatic(SecurityContextHolder.class);
             MockedStatic<RequestContextHolder> mockedRequest =
                     Mockito.mockStatic(RequestContextHolder.class)) {

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(null);
            mockedRequest.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            List<AuditLogResponseDto> result = auditLogService.getAllLogs("asc");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("1");
            assertThat(result.get(1).getId()).isEqualTo("2");

            verify(auditLogDao).findAll();
        }
    }

    @Test
    @DisplayName("getAllLogs(desc) harus mengembalikan list yang sudah di-sort desc menggunakan bubble sort")
    void getAllLogs_sortedDesc() {
        Date older = new Date(System.currentTimeMillis() - 10_000);
        Date newer = new Date(System.currentTimeMillis());

        AuditLog log1 = new AuditLog();
        log1.setId("1");
        log1.setTimestamp(older);

        AuditLog log2 = new AuditLog();
        log2.setId("2");
        log2.setTimestamp(newer);

        Mockito.doNothing().when(auditLogService).log(anyString(), anyString(), anyString());
        when(auditLogDao.findAll()).thenReturn(Arrays.asList(log1, log2));

        try (MockedStatic<SecurityContextHolder> mockedSecurity =
                     Mockito.mockStatic(SecurityContextHolder.class);
             MockedStatic<RequestContextHolder> mockedRequest =
                     Mockito.mockStatic(RequestContextHolder.class)) {

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(null);
            mockedRequest.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            List<AuditLogResponseDto> result = auditLogService.getAllLogs("desc");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("2");
            assertThat(result.get(1).getId()).isEqualTo("1");
        }
    }
}
