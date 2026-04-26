package com.example.myblog.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorHomeFlowTest extends BaseIntegrationTest {

    @Test
    void authorCanCreateFolderAndNoteThenSeeItOnHomepage() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(post("/api/folders")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Testing"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/notes")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"JUnit","markdownContent":"# JUnit","folderId":10}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders[0].notes[0].title", is("JUnit")));
    }
}

