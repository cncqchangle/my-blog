package com.example.myblog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorHomeFlowTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        mockMvc.perform(multipart("/api/notes")
                        .session(session)
                        .param("title", "JUnit")
                        .param("markdownContent", "# JUnit")
                        .param("folderId", "10"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders[0].notes[0].title", is("JUnit")))
                .andExpect(jsonPath("$.folders[0].notes[0].coverImageUrl", org.hamcrest.Matchers.startsWith("https://test-bucket.example.com/my-blog/generated-note-covers/alice/")))
                .andExpect(jsonPath("$.folders[0].notes[0].contentPreview", is("JUnit...")));
    }

    @Test
    void authorCanDeleteLastNoteThenDeleteFolder() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(delete("/api/notes/100").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/folders/10").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders[0].folderName", is("Recipes")));
    }

    @Test
    void deletingCreatedEmptyFolderRemovesItFromHomepage() throws Exception {
        var session = loginAs("alice");
        var createResult = mockMvc.perform(post("/api/folders")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Disposable"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long folderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("folderId").asLong();

        mockMvc.perform(delete("/api/folders/" + folderId).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders[0].folderName", is("Java Basics")))
                .andExpect(jsonPath("$.folders[1].folderName", is("Recipes")));
    }

    @Test
    void renamingFolderUpdatesItOnHomepage() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(put("/api/folders/10")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Java Advanced"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folders[0].folderId", is(10)))
                .andExpect(jsonPath("$.folders[0].folderName", is("Java Advanced")));
    }
}

