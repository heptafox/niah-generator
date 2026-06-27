package com.heptafox.niah.needle;

/**
 * Records where a needle landed in the delivered document — the canonical, format-independent
 * locators (char offset + depth %) the answer key exposes for scoring.
 */
public record InjectedNeedle(Needle needle, int segmentIndex, int depthPercent, int charOffset) {
}
