package com.library.libraryService.audit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.libraryService.audit.dto.AuditLogResponseDto;
import com.library.libraryService.audit.service.AuditLogService;
import com.library.libraryService.security.jwt.JwtUtil;
import com.library.libraryService.security.ratelimit.RateLimiterService;
import com.library.libraryService.user.controller.UserController;
import com.library.libraryService.user.dto.UserResponseDto;
import com.library.libraryService.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /users/getAll/{sortType} sukses get semua user")
    void getAllUsers_success() throws Exception {
        AuditLogResponseDto log1 = new AuditLogResponseDto();
        AuditLogResponseDto log2 = new AuditLogResponseDto();
        log1.setId("1");
        log2.setId("2");

        List<AuditLogResponseDto> responseList = List.of(log1, log2);

        Mockito.when(auditLogService.getAllLogs("asc"))
                .thenReturn(responseList);

        mockMvc.perform(
                        get("/audit/logs/{sortType}", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
