package com.heptafox.niah.render;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Renders the injected haystack as a standalone HTML page — this is what an agent crawls.
 * {@code noindex,nofollow} keeps search engines from indexing the intentionally-wrong needle facts;
 * an agent handed the direct URL still fetches it normally.
 */
@Component
public class HtmlRenderer implements DocumentRenderer {

    @Override
    public DocFormat format() {
        return DocFormat.HTML;
    }

    @Override
    public byte[] render(String title, List<String> paragraphs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n")
          .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
          .append("<meta name=\"robots\" content=\"noindex,nofollow\">\n")
          .append("<title>").append(escape(title)).append("</title>\n")
          .append("<style>body{font:18px/1.6 Georgia,\"Times New Roman\",serif;color:#1f2328;")
          .append("background:#f7f6f2;max-width:820px;margin:0 auto;padding:40px 24px}")
          .append("h1{font-size:2rem;line-height:1.2}p{margin:0 0 1.1rem}</style>\n")
          .append("</head>\n<body>\n<h1>").append(escape(title)).append("</h1>\n");
        for (String para : paragraphs) {
            sb.append("<p>").append(escape(para)).append("</p>\n");
        }
        sb.append("</body>\n</html>\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
