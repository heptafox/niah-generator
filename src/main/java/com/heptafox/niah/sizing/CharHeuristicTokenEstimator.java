package com.heptafox.niah.sizing;

import org.springframework.stereotype.Component;

import com.heptafox.niah.config.NiahProperties;

/**
 * Approximate token estimate using the standard English rule of thumb (~chars/4). Deliberately
 * approximate; swap in a real tokenizer (e.g. jtokkit) behind {@link TokenEstimator} if exact
 * counts are ever needed.
 */
@Component
public class CharHeuristicTokenEstimator implements TokenEstimator {

    private final int divisor;

    public CharHeuristicTokenEstimator(NiahProperties properties) {
        this.divisor = properties.tokenDivisor();
    }

    @Override
    public int estimate(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil((double) text.length() / divisor);
    }

    @Override
    public String note() {
        return "approximate (~chars/" + divisor + " estimate, not an exact tokenizer count)";
    }
}
