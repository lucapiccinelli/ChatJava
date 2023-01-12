package com.example.chatjava;

import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSocketSecurity
public class SecurityConfiguration {

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        return messages
            .nullDestMatcher().permitAll()
            .simpDestMatchers("/app/**").permitAll()
            .simpSubscribeDestMatchers("/topic/**").permitAll()
            .simpSubscribeDestMatchers("/user/**").permitAll()
            .anyMessage().denyAll()
            .build();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception{
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);
        return http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/basic/**").authenticated()
                )
                .httpBasic()
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/chat").hasAuthority("SCOPE_infolab")
                )
                .oauth2ResourceServer()
                .bearerTokenResolver(request -> {
                        String fromQueryString = request.getParameterMap().getOrDefault("access_token", new String[]{null})[0];
                        return fromQueryString == null ? new DefaultBearerTokenResolver().resolve(request) : fromQueryString;
                    }
                )
                .jwt().and().and()
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/**").permitAll())
                .anonymous()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwkSource jwkSource(){
        String jwkStr = """
        {
          "keys": [
            {
              "kty": "RSA",
              "e": "AQAB",
              "use": "sig",
              "kid": "Default-key",
              "alg": "RS256",
              "n": "x1ua_QcgasPT2iErEBNF_VSrsAcPqhgNpVY3WCynbySBGnXSU3iV4Wuxw2nBJv0ESIcpc5asjOqkXmCn4oJkxD2Jr4H840SrRr0g0pj2KeKnIkwB0XGSKQxKowhEGH4gbWzzMnfngZURBcR75JjVs1vvVLhCN1ZTLw8fRZi9v6f0n5MylS_fl_4UUUaikljtWx4hibDMva_5JD0TRjn-5CyiaojcI2HhsZ5TRnWGbIQ8Rmp6eVrrPn5ibqXOiJYbcFxEpRF7hqJ94Ws0Z8wXmfB_D7oQoKFwCoN-SQVqsJuiBjSmmIwdyhz1Vd4qyHNUDBe0EaHg7J2pVAQYSBYP6Q"
            }
          ]
        }
        """;
        return new StringJwkSource(jwkStr);
    }

    @Bean
    public JwtDecoder jwtDecoder(@Autowired JwkSource jwkSource) throws Exception{
        JWKSet jwkSet = JWKSet.parse(jwkSource.jwkStr());
        RSAPublicKey publicKey = jwkSet.getKeyByKeyId("Default-key").toRSAKey().toRSAPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

}

