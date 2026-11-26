package com.example.library.security;

import com.example.library.entity.LibraryUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class JwtTokens {
    public static final String ISSUER = "library-management-system";

    private final JwtEncoder encoder;
    private final Duration lifetime;
    private final Clock clock;

    public JwtTokens(JwtEncoder encoder, @Value("${library.jwt.lifetime}") Duration lifetime, Clock clock) {
        if (lifetime.isZero() || lifetime.isNegative()) {
            throw new IllegalArgumentException("JWT lifetime must be positive");
        }
        this.encoder = encoder;
        this.lifetime = lifetime;
        this.clock = clock;
    }

    public String issue(LibraryUser user) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim("role", user.getRole().name())
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
