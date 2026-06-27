package com.heptafox.niah.source;

import java.util.List;

/**
 * The cleaned, canonical haystack text extracted from the source PDF.
 *
 * @param pages   per-source-page cleaned text; each entry holds paragraphs separated by a blank line
 * @param fullText all pages joined with blank-line separators (paragraph-delimited)
 */
public record CanonicalDocument(List<String> pages, String fullText) {

    public CanonicalDocument {
        pages = List.copyOf(pages);
    }

    public int pageCount() {
        return pages.size();
    }
}
