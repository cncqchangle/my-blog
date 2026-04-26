package com.example.myblog.integration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeedbackTimingTest extends BaseIntegrationTest {

    @Test
    void homepageSearchAndNoteEndpointsRespondQuickly() throws Exception {
        var session = loginAs("alice");

        long home = timed(() -> mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk()));
        long search = timed(() -> mockMvc.perform(get("/api/users/search").session(session).param("q", "bo"))
                .andExpect(status().isOk()));
        long note = timed(() -> mockMvc.perform(get("/api/notes/100").session(session))
                .andExpect(status().isOk()));

        assertThat(home).isLessThan(1000L);
        assertThat(search).isLessThan(1000L);
        assertThat(note).isLessThan(1000L);
    }

    private long timed(ThrowingAction action) throws Exception {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }
}

