package com.example.chatjava;

import com.nimbusds.jwt.SignedJWT;

public interface JwtParser {
    SignedJWT parse(String token);
}
