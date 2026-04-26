package com.example.myblog.unit;

import com.example.myblog.service.MarkdownRenderService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownRenderServiceTest {

    private final MarkdownRenderService markdownRenderService = new MarkdownRenderService();

    @Test
    void rendersMarkdownAndRemovesUnsafeHtml() {
        String rendered = markdownRenderService.render("# Title\n<script>alert(1)</script>\n![x](https://example.com/a.png)");

        assertThat(rendered).contains("<h1>Title</h1>");
        assertThat(rendered).contains("<img");
        assertThat(rendered).doesNotContain("<script>");
    }
}

