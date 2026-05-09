package com.example.myblog.service;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class NotePreviewService {

    private static final int PREVIEW_LENGTH = 80;

    private final Parser parser = MarkdownSupport.newParser();
    private final HtmlRenderer renderer = MarkdownSupport.newRenderer();

    public String createPreview(String markdownContent) {
        if (markdownContent == null || markdownContent.isBlank()) {
            return "";
        }

        String html = renderer.render(parser.parse(MarkdownMathPreprocessor.preprocess(markdownContent)));
        Document document = Jsoup.parseBodyFragment(html);
        document.select("pre, code, img").remove();
        String text = document.text().replaceAll("\\s+", " ").trim();
        text = text.replaceAll("[.。！？!?,，;；:：]+$", "").trim();
        if (text.isEmpty()) {
            return "";
        }
        if (text.length() > PREVIEW_LENGTH) {
            return text.substring(0, PREVIEW_LENGTH) + "...";
        }
        return text + "...";
    }
}
