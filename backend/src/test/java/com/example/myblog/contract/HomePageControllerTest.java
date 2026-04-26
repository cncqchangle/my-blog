package com.example.myblog.contract;

import com.example.myblog.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomePageControllerTest extends BaseIntegrationTest {

    @Test
    void returnsLoggedInUsersHomepage() throws Exception {
        var session = loginAs("alice");

        mockMvc.perform(get("/api/users/me/home").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerAccount", is("alice")))
                .andExpect(jsonPath("$.isViewerOwner", is(true)))
                .andExpect(jsonPath("$.folders", hasSize(2)))
                .andExpect(jsonPath("$.folders[0].notes[0].title", is("Streams")));
    }
}

