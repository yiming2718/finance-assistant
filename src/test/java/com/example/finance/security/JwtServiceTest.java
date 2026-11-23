package com.example.finance.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private final JwtProperties props =
            new JwtProperties("0123456789-0123456789-0123456789-01", 30);
    private final JwtConfig config = new JwtConfig();
    private final JwtService service = new JwtService(config.jwtEncoder(props), props);
    private final JwtDecoder decoder = config.jwtDecoder(props);

    @Test
    void issuedToken_canBeDecoded_andCarriesUserId() {
        TokenResponse token = service.issue(42L);

        Jwt jwt = decoder.decode(token.accessToken());

        assertThat(jwt.getSubject()).isEqualTo("42");
        assertThat(jwt.getIssuer().toString()).isEqualTo(JwtConfig.ISSUER);
        assertThat(token.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void tamperedToken_isRejected() {
        String token = service.issue(42L).accessToken();
        String tampered = token.substring(0, token.length() - 3) + "xyz";

        assertThatThrownBy(() -> decoder.decode(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void tokenSignedWithAnotherSecret_isRejected() {
        JwtProperties otherProps = new JwtProperties("another-secret-another-secret-another-secret", 30);
        String foreign = new JwtService(config.jwtEncoder(otherProps), otherProps).issue(42L).accessToken();

        assertThatThrownBy(() -> decoder.decode(foreign)).isInstanceOf(JwtException.class);
    }
}