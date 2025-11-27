package com.library.libraryService.security.jwt;

import com.library.libraryService.common.handler.CustomAccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class JwtAuthenticationEntryPointTest {

    private final JwtAuthenticationEntryPoint commence = new JwtAuthenticationEntryPoint();

    @Test
    @DisplayName("commence set 403")
    void commence() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getRequestURI()).thenReturn("/api/protected");

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        AuthenticationException ex = mock(AuthenticationException.class);

        commence.commence(request, response, ex);

        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        verify(response).setContentType("application/json");
        verify(response).getWriter();
        verify(writer, atLeastOnce()).write(anyString());
    }
}
