package com.library.libraryService.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    @DisplayName("generateToken menghasilkan JWT yang tidak null dan formatnya benar")
    void generateToken_basic() {
        String token = jwtUtil.generateToken("123123", "fida", "ADMIN");

        assertNotNull(token);
        assertFalse(token.isBlank());

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("validateToken token hasil generateToken harus valid")
    void validateToken_validToken() {
        String token = jwtUtil.generateToken("123123", "fida", "EDITOR");

        boolean valid = jwtUtil.validateToken(token);

        assertTrue(valid);
    }

    @Test
    @DisplayName("validateToken token rusak/tampered harus invalid")
    void validateToken_invalidToken() {
        String token = jwtUtil.generateToken("123123", "fida", "VIEWER");
        String tampered = token + "x";

        assertFalse(jwtUtil.validateToken(tampered));

        assertFalse(jwtUtil.validateToken("token-acak-yang-bukan-jwt"));
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    @DisplayName("getUserId / getUsername / getRole harus bisa baca dari token")
    void getClaimsFromToken() {
        String userId = "123123";
        String username = "fida";
        String role = "SUPER_ADMIN";

        String token = jwtUtil.generateToken(userId, username, role);

        String extractedUserId = jwtUtil.getUserId(token);
        String extractedUsername = jwtUtil.getUsername(token);
        String extractedRole = jwtUtil.getRole(token);

        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(extractedUsername).isEqualTo(username);
        assertThat(extractedRole).isEqualTo(role);
    }
}
