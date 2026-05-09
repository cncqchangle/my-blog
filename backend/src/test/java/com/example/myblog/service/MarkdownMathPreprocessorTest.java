package com.example.myblog.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownMathPreprocessorTest {

    @Test
    void convertsInlineMathOutsideCodeSpans() {
        String processed = MarkdownMathPreprocessor.preprocess("Euler: $e^{i\\pi}+1=0$ and `code $x$`");

        assertThat(processed).contains("<span class=\"math-inline\">e^{i\\pi}+1=0</span>");
        assertThat(processed).contains("`code $x$`");
    }

    @Test
    void convertsBlockMathOutsideFencedCodeBlocks() {
        String processed = MarkdownMathPreprocessor.preprocess("""
                $$
                \\int_0^1 x^2 dx
                $$

                ```tex
                $$
                x^2
                $$
                ```
                """);

        assertThat(processed).contains("<span class=\"math-display\">\\int_0^1 x^2 dx</span>");
        assertThat(processed).contains("```tex");
        assertThat(processed).contains("x^2");
    }
}
