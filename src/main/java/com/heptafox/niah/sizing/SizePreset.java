package com.heptafox.niah.sizing;

/** A haystack size variant, controlled either by source-page count or by approximate token budget. */
public sealed interface SizePreset permits SizePreset.PagePreset, SizePreset.TokenPreset {

    /** Stable id fragment used in catalog entry ids, e.g. {@code pages-50} or {@code tokens-16k}. */
    String id();

    /** Human-friendly label for the UI. */
    String label();

    /** Compact descriptor recorded in the manifest, e.g. {@code pages=50} or {@code tokens~16000}. */
    String sizingDescriptor();

    /** Size measured in source-PDF pages. {@code full} means the entire book. */
    record PagePreset(String id, String label, int pages, boolean full) implements SizePreset {
        @Override
        public String sizingDescriptor() {
            return full ? "pages=full" : "pages=" + pages;
        }
    }

    /** Size measured by an approximate token budget. */
    record TokenPreset(String id, String label, int tokens) implements SizePreset {
        @Override
        public String sizingDescriptor() {
            return "tokens~" + tokens;
        }
    }
}
