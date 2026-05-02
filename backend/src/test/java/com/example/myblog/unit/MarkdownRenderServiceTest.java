package com.example.myblog.unit;

import com.example.myblog.service.MarkdownRenderService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownRenderServiceTest {

    private final MarkdownRenderService markdownRenderService = new MarkdownRenderService();

    @Test
    void rendersDashListItemsAsUnorderedList() {
        String rendered = markdownRenderService.render("- alpha\n- beta");

        assertThat(rendered).contains("<ul>");
        assertThat(rendered).contains("<li>alpha</li>");
        assertThat(rendered).contains("<li>beta</li>");
    }

    @Test
    void rendersAsteriskListItemsAsUnorderedList() {
        String rendered = markdownRenderService.render("* gamma\n* delta");

        assertThat(rendered).contains("<ul>");
        assertThat(rendered).contains("<li>gamma</li>");
        assertThat(rendered).contains("<li>delta</li>");
    }

    @Test
    void rendersGfmTables() {
        String rendered = markdownRenderService.render("| Name | Value |\n| --- | --- |\n| CPU | 80% |");

        assertThat(rendered).contains("<table>");
        assertThat(rendered).contains("<thead>");
        assertThat(rendered).contains("<tbody>");
        assertThat(rendered).contains("<th>Name</th>");
        assertThat(rendered).contains("<td>80%</td>");
    }

    @Test
    void rendersMarkdownAndRemovesUnsafeHtml() {
        String rendered = markdownRenderService.render("# Title\n<script>alert(1)</script>\n![x](https://example.com/a.png)");

        assertThat(rendered).contains("<h1>Title</h1>");
        assertThat(rendered).contains("<img");
        assertThat(rendered).doesNotContain("<script>");
    }

    @Test
    void rendersSingleNewlineAsLineBreak() {
        String rendered = markdownRenderService.render("alpha\nbeta");

        assertThat(rendered).contains("<p>alpha");
        assertThat(rendered).contains("<br>");
        assertThat(rendered).contains("beta</p>");
    }
}

