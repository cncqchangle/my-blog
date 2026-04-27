package com.example.myblog.unit;

import com.example.myblog.service.NotePreviewService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotePreviewServiceTest {

    private final NotePreviewService notePreviewService = new NotePreviewService();

    @Test
    void createsPreviewFromMarkdownText() {
        assertThat(notePreviewService.createPreview("# Title\nUseful stream notes."))
                .isEqualTo("Title Useful stream notes...");
    }

    @Test
    void removesImageAndCodeContentFromPreview() {
        assertThat(notePreviewService.createPreview("![cover](https://example.com/a.png)\n```java\nSystem.out.println(1);\n```\nBody text"))
                .isEqualTo("Body text...");
    }

    @Test
    void createsReadablePreviewFromTableMarkdown() {
        String preview = notePreviewService.createPreview("| Name | Value |\n| --- | --- |\n| CPU | 80% |");

        assertThat(preview).isEqualTo("Name Value CPU 80%...");
        assertThat(preview).doesNotContain("|");
    }

    @Test
    void returnsEmptyStringWhenVisibleTextIsMissing() {
        assertThat(notePreviewService.createPreview("![cover](https://example.com/a.png)")).isEmpty();
    }

    @Test
    void truncatesLongPreviewAndAppendsEllipsis() {
        String preview = notePreviewService.createPreview("a".repeat(120));

        assertThat(preview).hasSize(83);
        assertThat(preview).endsWith("...");
    }
}
