package com.glassburet.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void generateTokenAndParseClaimsRoundTrip() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 60_000L);

        String token = jwtUtil.generateToken("Ada", "ROLE_OWNER");
        Claims claims = jwtUtil.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo("Ada");
        assertThat(claims.get("role")).isEqualTo("ROLE_OWNER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
