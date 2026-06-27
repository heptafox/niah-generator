package com.heptafox.niah.sizing;

import org.springframework.stereotype.Component;

import com.heptafox.niah.source.CanonicalDocument;

/**
 * Truncates the canonical haystack to a requested size <em>before</em> needles are injected, so the
 * needles always survive and sit at the correct relative depth within the delivered document.
 */
@Component
public class DocumentSlicer {

    private final TokenEstimator tokenEstimator;

    public DocumentSlicer(TokenEstimator tokenEstimator) {
        this.tokenEstimator = tokenEstimator;
    }

    /** Returns the paragraph-delimited haystack text for the given size preset. */
    public String slice(CanonicalDocument document, SizePreset preset) {
        return switch (preset) {
            case SizePreset.PagePreset page -> slicePages(document, page);
            case SizePreset.TokenPreset token -> sliceTokens(document, token.tokens());
        };
    }

    private String slicePages(CanonicalDocument document, SizePreset.PagePreset page) {
        if (page.full()) {
            return document.fullText();
        }
        int count = Math.min(page.pages(), document.pageCount());
        return String.join("\n\n", document.pages().subList(0, count));
    }

    private String sliceTokens(CanonicalDocument document, int budget) {
        String[] paragraphs = document.fullText().split("\n\n");
        StringBuilder out = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (out.length() > 0 && tokenEstimator.estimate(out.toString()) >= budget) {
                break;
            }
            if (out.length() > 0) {
                out.append("\n\n");
            }
            out.append(paragraph);
        }
        return out.toString();
    }
}
