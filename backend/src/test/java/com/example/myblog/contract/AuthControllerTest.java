package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseIntegrationTest {

    @Test
    void registerCreatesAccountAndSessionOnSuccess() throws Exception {
        var result = mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"account":"charlie","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account", is("charlie")))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
    }

    @Test
    void registerReturnsConflictWhenAccountExists() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"account":"ALICE","password":"password123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    void loginReturnsSessionOnSuccess() throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"account":"alice","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account", is("alice")))
                .andReturn();

        assertThat(result.getRequest().getSession(false)).isNotNull();
    }

    @Test
    void loginReturnsUnauthorizedOnBadPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"account":"alice","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    void logoutClearsSession() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isNoContent());
    }
}
