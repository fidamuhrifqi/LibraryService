package com.library.libraryService.common.handler;

import com.library.libraryService.common.dto.ErrorResponse;
import com.library.libraryService.user.exception.AccountLockedException;
import com.library.libraryService.user.exception.InvalidLoginException;
import com.library.libraryService.user.exception.InvalidOtpException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleInvalidLogin harus return 401 dan ErrorResponse yang sesuai")
    void handleInvalidLogin_shouldReturnUnauthorized() {
        InvalidLoginException ex = new InvalidLoginException("username atau password salah");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/users/login");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidLogin(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), body.getStatus());
        assertEquals("INVALID_CREDENTIALS", body.getError());
        assertEquals("username atau password salah", body.getMessage());
        assertEquals("/users/login", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("handleAccountLocked harus return 423 dan ErrorResponse yang sesuai")
    void handleAccountLocked_shouldReturn423() {
        AccountLockedException ex = new AccountLockedException("Akun terkunci");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/users/login");

        ResponseEntity<ErrorResponse> response = handler.handleAccountLocked(ex, request);

        assertEquals(423, response.getStatusCode().value());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(423, body.getStatus());
        assertEquals("ACCOUNT_LOCKED", body.getError());
        assertEquals("Akun terkunci", body.getMessage());
        assertEquals("/users/login", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("handleInvalidOtp harus return 401 dan ErrorResponse yang sesuai")
    void handleInvalidOtp_shouldReturnUnauthorized() {
        InvalidOtpException ex = new InvalidOtpException("OTP salah");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/users/verify-otp");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidOtp(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), body.getStatus());
        assertEquals("INVALID_OTP", body.getError());
        assertEquals("OTP salah", body.getMessage());
        assertEquals("/users/verify-otp", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("handleValidationException(MethodArgumentNotValidException) harus return 400 dengan message & errors field")
    void handleMethodArgumentNotValid_shouldReturnBadRequest() {
        // mock BindingResult dengan 2 field error
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("objectName", "username", "username wajib diisi");
        FieldError error2 = new FieldError("objectName", "email", "email tidak valid");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Payload tidak valid", body.get("message"));
    }

    @Test
    @DisplayName("handleValidationException(ConstraintViolationException) harus return 400 dengan message & errors field")
    void handleConstraintViolation_shouldReturnBadRequest() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = (ConstraintViolation<Object>) mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("getAllArticles.sortType");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("sortType tidak boleh kosong");

        ConstraintViolationException ex =
                new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Path variable tidak valid", body.get("message"));
    }

    @Test
    @DisplayName("handleRuntimeException harus return 500 dengan body standar error")
    void handleRuntimeException_shouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("error 123");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/some/api");

        ResponseEntity<Map<String, Object>> response =
                handler.handleRuntimeException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("error 123", body.get("message"));
        assertEquals("/some/api", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("handleRuntimeException harus return 500 dengan body standar error tanpa messege")
    void handleRuntimeException_shouldReturnInternalServerError2() {
        RuntimeException ex = new RuntimeException();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/some/api");

        ResponseEntity<Map<String, Object>> response =
                handler.handleRuntimeException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("Terjadi kesalahan pada server", body.get("message"));
        assertEquals("/some/api", body.get("path"));
        assertNotNull(body.get("timestamp"));
    }
}
