package com.heptafox.niah.render;

import java.util.List;

/** Renders a paragraph-delimited document to bytes in a specific {@link DocFormat}. */
public interface DocumentRenderer {

    DocFormat format();

    byte[] render(String title, List<String> paragraphs);
}
