package com.heptafox.niah.needle;

/** A single counterfactual fact injected into the haystack, with its ground-truth Q&A. */
public record Needle(String id, String text, String question, String groundTruthAnswer) {
}
