package com.heptafox.niah.needle;

import java.util.List;

/**
 * The haystack text after injection, plus the recorded position of every needle.
 *
 * @param paragraphs the final document as a paragraph list (needles are standalone paragraphs)
 * @param injected   where each needle landed, in document order
 */
public record InjectionResult(List<String> paragraphs, List<InjectedNeedle> injected) {

    public InjectionResult {
        paragraphs = List.copyOf(paragraphs);
        injected = List.copyOf(injected);
    }
}
