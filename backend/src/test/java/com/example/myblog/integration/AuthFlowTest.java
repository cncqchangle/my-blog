package com.example.myblog.integration;

import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired(required = false)
    @Qualifier("springSessionRepositoryFilter")
    private Filter springSessionRepositoryFilter;

    private MockMvc sessionMockMvc;

    @BeforeEach
    void setUpSessionMockMvc() {
        if (springSessionRepositoryFilter == null) {
            sessionMockMvc = null;
            return;
        }
        sessionMockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(springSessionRepositoryFilter)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void loginCreatesUsableSession() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerAccount", is("alice")))
                .andExpect(jsonPath("$.isViewerOwner", is(true)));
    }

    @Test
    void loginCookieCanBeReusedAcrossFreshRequestContext() throws Exception {
        assertThat(sessionMockMvc).isNotNull();

        var loginResult = sessionMockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"account":"alice","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String cookieValue = extractSessionCookieValue(loginResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE));
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM SPRING_SESSION", Integer.class))
                .isNotNull()
                .isGreaterThan(0);

        sessionMockMvc.perform(get("/api/users/me/home").cookie(new Cookie("JSESSIONID", cookieValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerAccount", is("alice")))
                .andExpect(jsonPath("$.isViewerOwner", is(true)));
    }

    @Test
    void authenticatedRequestsRefreshPersistentSessionCookie() throws Exception {
        assertThat(sessionMockMvc).isNotNull();

        var loginResult = sessionMockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"account":"alice","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String cookieValue = extractSessionCookieValue(loginResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE));

        var firstRequest = sessionMockMvc.perform(get("/api/users/me/home").cookie(new Cookie("JSESSIONID", cookieValue)))
                .andExpect(status().isOk())
                .andReturn();
        List<String> firstSetCookies = firstRequest.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertContainsPersistentSessionCookie(firstSetCookies);

        String refreshedCookieValue = extractSessionCookieValue(firstSetCookies);
        List<String> secondSetCookies = sessionMockMvc.perform(get("/api/users/me/home").cookie(new Cookie("JSESSIONID", refreshedCookieValue)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getHeaders(HttpHeaders.SET_COOKIE);
        assertContainsPersistentSessionCookie(secondSetCookies);
    }

    private static String extractSessionCookieValue(List<String> setCookieHeaders) {
        return setCookieHeaders.stream()
                .filter(cookie -> cookie.startsWith("JSESSIONID="))
                .findFirst()
                .map(cookie -> cookie.substring("JSESSIONID=".length(), cookie.indexOf(';')))
                .orElseThrow(() -> new AssertionError("JSESSIONID cookie was not found"));
    }

    private static void assertContainsPersistentSessionCookie(List<String> setCookieHeaders) {
        assertThat(setCookieHeaders)
                .anySatisfy(cookie -> {
                    assertThat(cookie).contains("JSESSIONID=");
                    assertThat(cookie).contains("Max-Age=604800");
                });
    }
}

