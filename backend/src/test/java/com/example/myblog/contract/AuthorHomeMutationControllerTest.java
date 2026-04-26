package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorHomeMutationControllerTest extends BaseIntegrationTest {

    @Test
    void createsFolderForCurrentUser() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(post("/api/folders")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Algorithms"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Algorithms")));
    }

    @Test
    void createsNoteInOwnedFolder() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(post("/api/notes")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Queues","markdownContent":"# Queues","folderId":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.renderedHtml", is("<h1>Queues</h1>")));
    }

    @Test
    void rejectsCreatingNoteInForeignFolder() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(post("/api/notes")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Nope","markdownContent":"text","folderId":20}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }
}

