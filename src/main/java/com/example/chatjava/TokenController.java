package com.example.chatjava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TokenController {
    private String basicAuth;
    private final String username;
    private final String password;
    private final RestTemplate restTemplate;

    public TokenController(
        @Value("${infolab.chat.token.basicauth}")
        String basicAuth,
        @Value("${infolab.chat.token.username}")
        String username,
        @Value("${infolab.chat.token.password}")
        String password,
        @Autowired RestTemplate restTemplate) {
        this.basicAuth = basicAuth;
        this.username = username;
        this.password = password;
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/token")
    public Token token() {
        ResponseEntity<Token> authorization = restTemplate.exchange(RequestEntity
            .post("https://demo.staging.stella.cgm.com/oauth/token")
            .header("Authorization", String.format("Basic %s", basicAuth))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(String.format("grant_type=password&username=%s&password=%s", username, password)),
            Token.class
        );

        return authorization.getBody();
    }
}

record Token(String access_token, String token_type, String refresh_token, Integer expires_in, String scope){}