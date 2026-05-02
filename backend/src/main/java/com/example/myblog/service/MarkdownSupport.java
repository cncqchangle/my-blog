package com.example.myblog.service;

import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

final class MarkdownSupport {

    private static final List<Extension> EXTENSIONS = List.of(TablesExtension.create());

    private MarkdownSupport() {
    }

    static Parser newParser() {
        return Parser.builder()
                .extensions(EXTENSIONS)
                .build();
    }

    static HtmlRenderer newRenderer() {
        return HtmlRenderer.builder()
                .extensions(EXTENSIONS)
                .softbreak("<br>\n")
                .build();
    }
}
