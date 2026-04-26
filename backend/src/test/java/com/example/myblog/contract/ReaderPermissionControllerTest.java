package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

        mockMvc.perform(put("/api/notes/100")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Hack","markdownContent":"hack","folderId":20}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("FORBIDDEN")));
    }
}

