package com.example.myblog.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends BaseIntegrationTest {

    @Test
    void loginCreatesUsableSession() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerAccount", is("alice")))
                .andExpect(jsonPath("$.isViewerOwner", is(true)));
    }
}

