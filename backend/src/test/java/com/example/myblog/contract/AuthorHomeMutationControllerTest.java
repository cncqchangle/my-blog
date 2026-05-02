package com.example.myblog.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorHomeMutationControllerTest extends BaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void rejectsCreatingFolderWithDuplicateName() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(post("/api/folders")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Java Basics"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("Folder name already exists")));
    }

    @Test
    void renamesOwnedFolder() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(put("/api/folders/10")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Java Advanced"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folderId", is(10)))
                .andExpect(jsonPath("$.name", is("Java Advanced")));
    }

    @Test
    void rejectsRenamingFolderToDuplicateName() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(put("/api/folders/10")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Recipes"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", is("Folder name already exists")));
    }

    @Test
    void createsNoteInOwnedFolder() throws Exception {
        var session = loginAs("alice");
        var coverImage = new MockMultipartFile("coverImage", "cover.png", "image/png", "cover".getBytes());

        mockMvc.perform(multipart("/api/notes")
                        .session(session)
                        .file(coverImage)
                        .param("title", "Queues")
                        .param("markdownContent", "# Queues")
                        .param("folderId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.renderedHtml", is("<h1>Queues</h1>")))
                .andExpect(jsonPath("$.coverImageUrl", startsWith("https://test-bucket.example.com/my-blog/note-covers/alice/")));
    }

    @Test
    void createsNoteWithRenderedUnorderedList() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(multipart("/api/notes")
                        .session(session)
                        .param("title", "Checklist")
                        .param("markdownContent", "- alpha\n* beta")
                        .param("folderId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.renderedHtml", containsString("<ul>")))
                .andExpect(jsonPath("$.renderedHtml", containsString("<li>alpha</li>")))
                .andExpect(jsonPath("$.renderedHtml", containsString("<li>beta</li>")));
    }

    @Test
    void createsNoteWithDefaultCoverWhenImageMissing() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(multipart("/api/notes")
                        .session(session)
                        .param("title", "Queues")
                        .param("markdownContent", "# Queues")
                        .param("folderId", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.coverImageUrl", startsWith("https://test-bucket.example.com/my-blog/generated-note-covers/alice/")));
    }

    @Test
    void rejectsCreatingNoteInForeignFolder() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(multipart("/api/notes")
                        .session(session)
                        .param("title", "Nope")
                        .param("markdownContent", "text")
                        .param("folderId", "20"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }

    @Test
    void rejectsUnsupportedCoverImageType() throws Exception {
        var session = loginAs("alice");
        var coverImage = new MockMultipartFile("coverImage", "cover.txt", "text/plain", "not-an-image".getBytes());

        mockMvc.perform(multipart("/api/notes")
                        .session(session)
                        .file(coverImage)
                        .param("title", "Queues")
                        .param("markdownContent", "# Queues")
                        .param("folderId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    void rejectsOversizedCoverImage() throws Exception {
        var session = loginAs("alice");
        var oversizedImage = new MockMultipartFile("coverImage", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1]);

        mockMvc.perform(multipart("/api/notes")
                        .session(session)
                        .file(oversizedImage)
                        .param("title", "Queues")
                        .param("markdownContent", "# Queues")
                        .param("folderId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")));
    }

    @Test
    void authorCanDeleteOwnedNote() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(delete("/api/notes/100").session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnsNotFoundWhenDeletingMissingNote() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(delete("/api/notes/999").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }

    @Test
    void authorCanDeleteEmptyFolder() throws Exception {
        var session = loginAs("alice");
        var createResult = mockMvc.perform(post("/api/folders")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"Temporary"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long folderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("folderId").asLong();

        mockMvc.perform(delete("/api/folders/" + folderId).session(session))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnsConflictWhenDeletingNonEmptyFolder() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(delete("/api/folders/10").session(session))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    void returnsNotFoundWhenDeletingMissingFolder() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(delete("/api/folders/999").session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }
}

