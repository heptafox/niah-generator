package com.heptafox.niah.catalog;

import java.util.List;

/** Machine-readable answer key bundled (optionally) alongside a downloaded document. */
public record Manifest(
        String documentId,
        String title,
        String format,
        String sizing,
        String placementMode,
        int approxTokens,
        int approxPages,
        String tokenEstimateNote,
        String offsetNote,
        List<NeedleRecord> needles) {

    /** One injected needle's text, ground-truth Q&A, and position. */
    public record NeedleRecord(
            String id,
            String text,
            String question,
            String groundTruthAnswer,
            int segmentIndex,
            int depthPercent,
            int charOffset) {
    }
}
