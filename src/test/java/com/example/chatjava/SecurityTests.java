package com.example.chatjava;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SecurityTestsController.class})
public class SecurityTests {

    private MockMvc client;

    public SecurityTests(@Autowired MockMvc client) {
        this.client = client;
    }

    @Test
    void callToAnUnprotectedRouteEndPointShouldSucceed() throws Exception {
        client
            .perform(get("/unprotected/test"))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello"));
    }

    @Test
    void callToASecuredRouteEndPointShouldFail() throws Exception {
        client
            .perform(get("/api/test"))
            .andExpect(status().isUnauthorized());
    }
}
