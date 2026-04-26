package com.example.myblog.integration;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class AccessibilitySmokeTest extends BaseIntegrationTest {

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void staticPagesExposeLabelsAndLiveRegions() throws Exception {
        String homeHtml = resourceLoader.getResource("classpath:static/pages/home.html")
                .getContentAsString(StandardCharsets.UTF_8);
        String noteHtml = resourceLoader.getResource("classpath:static/pages/note.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(homeHtml).contains("label for=\"search-query\"");
        assertThat(homeHtml).contains("aria-live=\"polite\"");
        assertThat(noteHtml).contains("role=\"group\"");
        assertThat(noteHtml).contains("aria-live=\"polite\"");
    }
}

