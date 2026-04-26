package com.example.myblog.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteEditingFlowTest extends BaseIntegrationTest {

    @Test
    void authorCanEditAndReloadNote() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(put("/api/notes/100")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Streams Deep Dive","markdownContent":"# Streams Deep Dive\\n![img](https://example.com/demo.png)","folderId":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.renderedHtml", containsString("<img")));

        mockMvc.perform(get("/api/notes/100").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Streams Deep Dive")))
                .andExpect(jsonPath("$.isEditable", is(true)));
    }
}
