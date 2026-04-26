package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteUpdateControllerTest extends BaseIntegrationTest {

    @Test
    void authorCanUpdateOwnedNote() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(put("/api/notes/100")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Streams Updated","markdownContent":"# Updated","folderId":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId", is(100)))
                .andExpect(jsonPath("$.renderedHtml", containsString("<h1>Updated</h1>")));
    }
}

