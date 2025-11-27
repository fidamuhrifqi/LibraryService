package com.library.libraryService.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterFilterTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimiterFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        when(auth.isAuthenticated()).thenReturn(true);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Path excluded langsung doFilter, tanpa hit rateLimiterService")
    void excludedPath_shouldBypassRateLimit() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimiterService);
    }

    @Test
    @DisplayName("User terautentikasi & allowed pakai key USER_username, set header remaining, lalu doFilter")
    void authenticatedUser_allowed() throws ServletException, IOException {
        mockAuthenticatedUser("fida");
        when(request.getRequestURI()).thenReturn("/articles/getAll");

        when(rateLimiterService.isAllowed("USER_fida")).thenReturn(true);
        when(rateLimiterService.getRemaining("USER_fida")).thenReturn(7);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed("USER_fida");
        verify(rateLimiterService).getRemaining("USER_fida");

        verify(response).setHeader("X-RateLimit-Remaining", "7");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("User tidak login & allowed pakai key IP_xxx, set header remaining, lalu doFilter")
    void anonymousUser_allowed() throws ServletException, IOException {
        // tidak set SecurityContext -> anonymous
        when(request.getRequestURI()).thenReturn("/articles/getAll");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10");

        String expectedKey = "IP_203.0.113.10";

        when(rateLimiterService.isAllowed(expectedKey)).thenReturn(true);
        when(rateLimiterService.getRemaining(expectedKey)).thenReturn(3);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed(expectedKey);
        verify(rateLimiterService).getRemaining(expectedKey);
        verify(response).setHeader("X-RateLimit-Remaining", "3");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Jika X-Forwarded-For kosong, pakai remoteAddr sebagai key IP_")
    void anonymousUser_useRemoteAddrWhenNoXForwardedFor() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/articles/getAll");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.5");

        String expectedKey = "IP_10.0.0.5";

        when(rateLimiterService.isAllowed(expectedKey)).thenReturn(true);
        when(rateLimiterService.getRemaining(expectedKey)).thenReturn(9);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimiterService).isAllowed(expectedKey);
        verify(rateLimiterService).getRemaining(expectedKey);
        verify(response).setHeader("X-RateLimit-Remaining", "9");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Jika tidak allowed set 429, header Retry-After, dan tidak memanggil filterChain")
    void tooManyRequests_shouldReturn429() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/articles/getAll");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.11");

        String key = "IP_203.0.113.11";

        when(rateLimiterService.isAllowed(key)).thenReturn(false);
        when(rateLimiterService.getRemaining(key)).thenReturn(0);
        when(rateLimiterService.getWindowMs()).thenReturn(60_000L);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-RateLimit-Remaining", "0");
        verify(response).setHeader("Retry-After", "60");

        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).setContentType("application/json;charset=UTF-8");

        verify(filterChain, never()).doFilter(request, response);
    }
}
