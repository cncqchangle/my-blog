package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReaderPermissionControllerTest extends BaseIntegrationTest {

    @Test
    void readerGetsReadOnlyNoteDetail() throws Exception {
        var session = loginAs("bob");

        mockMvc.perform(get("/api/notes/100").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEditable", is(false)));
    }

    @Test
    void readerCannotEditAnotherUsersNote() throws Exception {
        var session = loginAs("bob");

        mockMvc.perform(multipart("/api/notes/100")
                        .session(session)
                        .param("title", "Hack")
                        .param("markdownContent", "hack")
                        .param("folderId", "20")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }

    @Test
    void readerCannotDeleteAnotherUsersNote() throws Exception {
        var session = loginAs("bob");

        mockMvc.perform(delete("/api/notes/100").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }

    @Test
    void readerCannotDeleteAnotherUsersFolder() throws Exception {
        var session = loginAs("bob");

        mockMvc.perform(delete("/api/folders/10").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }
}

