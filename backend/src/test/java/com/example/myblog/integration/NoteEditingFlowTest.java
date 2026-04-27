package com.example.myblog.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteEditingFlowTest extends BaseIntegrationTest {

    @Test
    void authorCanEditAndReloadNote() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(multipart("/api/notes/100")
                        .session(session)
                        .param("title", "Streams Deep Dive")
                        .param("markdownContent", "# Streams Deep Dive\n![img](https://example.com/demo.png)")
                        .param("folderId", "10")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.renderedHtml", containsString("<img")));

        mockMvc.perform(get("/api/notes/100").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Streams Deep Dive")))
                .andExpect(jsonPath("$.coverImageUrl", org.hamcrest.Matchers.startsWith("https://test-bucket.example.com/my-blog/generated-note-covers/alice/")))
                .andExpect(jsonPath("$.isEditable", is(true)));
    }

    @Test
    void authorCanDeleteNoteAndReloadReturnsNotFound() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(delete("/api/notes/100").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/notes/100").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }
}
