package com.example.chatjava;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyCsrfTokenRepository {
    private final List<CsrfToken> tokens = new ArrayList<>();

    public void append(CsrfToken token) {
        tokens.add(token);
    }

    public CsrfToken last() {
        return tokens.get(tokens.size() - 1);
    }
}
