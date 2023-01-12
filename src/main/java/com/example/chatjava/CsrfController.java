package com.example.chatjava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @Autowired
    private MyCsrfTokenRepository tokenRepository;

    @RequestMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        tokenRepository.append(token);
        return token;
    }
}

