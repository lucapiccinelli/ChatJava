package com.example.chatjava;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SecurityTestsController.class})
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
public class SecurityTests {

    @TestConfiguration
    public static class TestConfig {
        @Bean
        @Primary
        public JwtDecoder jwtDecoder(){
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
            return new SimpleJwtDecoder(new StringJwkSource(jwkStr));
        }
    }

    @Autowired
    private MockMvc client;

    @Test
    void callToAnUnprotectedRouteEndPointShouldSucceed() throws Exception {
        client
            .perform(get("/public/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello"));
    }

    @Test
    void callToASecuredRouteEndPointShouldFail() throws Exception {
        client
            .perform(get("/basic/test"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void callToASecuredRouteEndPointShouldFailWithAnAuthenticatedUserShouldBeOk() throws Exception {
        client
            .perform(get("/basic/test"))
            .andExpect(status().isOk());
    }

    @Test
    void callToAnApiEndPointWithAValidJwtShouldBeOk() throws Exception {
        client
            .perform(get("/api/test").header("Authorization", String.format("Bearer %s", TestConstants.Token)))
            .andExpect(status().isOk());
    }

}
