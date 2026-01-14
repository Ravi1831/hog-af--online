package com.ravi.hogwartsartifact.security;

import com.ravi.hogwartsartifact.hogwartsuser.MyUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtProviders {

    private final JwtEncoder jwtEncoder;

    public JwtProviders(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String createToken(Authentication authentication) {
        Instant instant = Instant.now();
        long expiresIn = 2;

        //prepare a claim called authorities
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(expiresIn, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .claim("userId", ((MyUserPrincipal) (Objects.requireNonNull(authentication.getPrincipal()))).getHogwartsUser().getId())
                .build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
