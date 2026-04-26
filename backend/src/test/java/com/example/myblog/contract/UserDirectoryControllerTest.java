package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserDirectoryControllerTest extends BaseIntegrationTest {

    @Test
    void searchesOtherUsersByPartialAccount() throws Exception {
        var session = loginAs("bob");

        mockMvc.perform(get("/api/users/search").session(session).param("q", "al"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(2)))
                .andExpect(jsonPath("$.results[0].account", is("alice")));
    }

    @Test
    void loadsForeignHomepageInReadOnlyMode() throws Exception {
        var session = loginAs("bob");

        mockMvc.perform(get("/api/users/alice/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerAccount", is("alice")))
                .andExpect(jsonPath("$.isViewerOwner", is(false)));
    }
}
