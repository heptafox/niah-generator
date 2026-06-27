package com.heptafox.niah.render;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MarkdownRenderer implements DocumentRenderer {

    @Override
    public DocFormat format() {
        return DocFormat.MD;
    }

    @Override
    public byte[] render(String title, List<String> paragraphs) {
        StringBuilder md = new StringBuilder();
        md.append("# ").append(title).append("\n\n");
        md.append(String.join("\n\n", paragraphs));
        md.append('\n');
        return md.toString().getBytes(StandardCharsets.UTF_8);
    }
}
