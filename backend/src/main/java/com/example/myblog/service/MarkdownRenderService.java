package com.example.myblog.service;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class MarkdownRenderService {

    private final Parser parser = MarkdownSupport.newParser();
    private final HtmlRenderer renderer = MarkdownSupport.newRenderer();
    private final Safelist safelist = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "h4", "h5", "h6", "pre", "code")
            .addTags("ul", "ol", "li", "table", "thead", "tbody", "tr", "th", "td")
            .addAttributes("img", "src", "alt", "title")
            .addProtocols("img", "src", "http", "https");

    public String render(String markdown) {
        String html = renderer.render(parser.parse(markdown == null ? "" : markdown));
        return Jsoup.clean(html, safelist);
    }
}

