package com.example.myblog.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReaderSearchFlowTest extends BaseIntegrationTest {

    @Test
    void secondUserCanSearchBrowseAndReadOtherUsersNotes() throws Exception {
        var session = loginAs("bob");

        mockMvc.perform(get("/api/users/search").session(session).param("q", "ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].account", is("alice")));

        mockMvc.perform(get("/api/users/alice/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders[0].notes[0].noteId", is(100)));

        mockMvc.perform(get("/api/notes/100").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorAccount", is("alice")))
                .andExpect(jsonPath("$.isEditable", is(false)));
    }
}

