package com.heptafox.niah.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bound from the {@code niah.*} keys in application.yaml. Drives the source location, the size
 * preset matrix, token estimation, and the counterfactual needle pool.
 */
@ConfigurationProperties(prefix = "niah")
public record NiahProperties(
        String sourcePdf,
        int segmentCount,
        int tokenDivisor,
        List<Integer> pagePresets,
        List<Integer> tokenPresets,
        List<NeedleSpec> needles) {

    public NiahProperties {
        if (segmentCount <= 0) {
            segmentCount = 3;
        }
        if (tokenDivisor <= 0) {
            tokenDivisor = 4;
        }
        pagePresets = pagePresets == null ? List.of() : List.copyOf(pagePresets);
        tokenPresets = tokenPresets == null ? List.of() : List.copyOf(tokenPresets);
        needles = needles == null ? List.of() : List.copyOf(needles);
    }

    /** A single counterfactual needle definition with its ground-truth Q&A. */
    public record NeedleSpec(String id, String text, String question, String groundTruthAnswer) {
    }
}
