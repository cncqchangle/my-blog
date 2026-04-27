package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteUpdateControllerTest extends BaseIntegrationTest {

    @Test
    void authorCanUpdateOwnedNote() throws Exception {
        var session = loginAs("alice");
        var coverImage = new MockMultipartFile("coverImage", "fresh-cover.png", "image/png", "fresh".getBytes());

        mockMvc.perform(multipart("/api/notes/100")
                        .session(session)
                        .file(coverImage)
                        .param("title", "Streams Updated")
                        .param("markdownContent", "# Updated")
                        .param("folderId", "10")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId", is(100)))
                .andExpect(jsonPath("$.renderedHtml", containsString("<h1>Updated</h1>")))
                .andExpect(jsonPath("$.coverImageUrl", startsWith("https://test-bucket.example.com/my-blog/note-covers/alice/")));
    }

    @Test
    void updateKeepsExistingCoverWhenImageMissing() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(multipart("/api/notes/101")
                        .session(session)
                        .param("title", "Soup Updated")
                        .param("markdownContent", "Warm and cozy")
                        .param("folderId", "11")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl", is("https://static.example.com/covers/soup.png")));
    }

    @Test
    void updateRegeneratesGeneratedCoverWhenImageMissing() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(multipart("/api/notes/100")
                        .session(session)
                        .param("title", "Streams Updated")
                        .param("markdownContent", "Warm and cozy")
                        .param("folderId", "10")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImageUrl", startsWith("https://test-bucket.example.com/my-blog/generated-note-covers/alice/")));
    }

    @Test
    void authorCanUpdateOwnedNoteWithRenderedTable() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(multipart("/api/notes/100")
                        .session(session)
                        .param("title", "Streams Updated")
                        .param("markdownContent", "| Framework | Version |\n| --- | --- |\n| Spring Boot | 4.0.6 |")
                        .param("folderId", "10")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.renderedHtml", containsString("<table>")))
                .andExpect(jsonPath("$.renderedHtml", containsString("<th>Framework</th>")))
                .andExpect(jsonPath("$.renderedHtml", containsString("<td>Spring Boot</td>")));
    }
}

