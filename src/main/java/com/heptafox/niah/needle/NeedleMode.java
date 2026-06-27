package com.heptafox.niah.needle;

/** How visible an injected needle is within the surrounding prose. */
public enum NeedleMode {

    /** Needle sits as its own standalone paragraph (easy baseline: chunkers isolate it cleanly). */
    ISOLATED,
    /** Needle is blended in as a sentence inside the surrounding paragraph (realistic "buried" test). */
    EMBEDDED;

    /** Lenient parse used for the {@code mode} query param; defaults to {@link #ISOLATED}. */
    public static NeedleMode from(String value) {
        if (value == null || value.isBlank()) {
            return ISOLATED;
        }
        return NeedleMode.valueOf(value.trim().toUpperCase());
    }

    public String slug() {
        return name().toLowerCase();
    }
}
