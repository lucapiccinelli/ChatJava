package com.example.chatjava;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SimpleJwtDecoder implements JwtDecoder {

    private final JwkSource jwkSource;

    public SimpleJwtDecoder(JwkSource jwkSource) {

        this.jwkSource = jwkSource;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            JWKSet jwkSet = JWKSet.parse(jwkSource.jwkStr());
            RSAPublicKey publicKey = jwkSet.getKeyByKeyId("Default-key").toRSAKey().toRSAPublicKey();
            SignedJWT jwt = SignedJWT.parse(token);
            jwt.verify(new RSASSAVerifier(publicKey));

            return Jwt.withTokenValue(token)
                .headers(h -> h.putAll(jwt.getHeader().toJSONObject()))
                .claims(c -> {
                    try {
                        c.putAll(jwt.getJWTClaimsSet().toJSONObject());
                        c.put("exp", Instant.now().plus(30, ChronoUnit.SECONDS));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .build();
        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }
}
