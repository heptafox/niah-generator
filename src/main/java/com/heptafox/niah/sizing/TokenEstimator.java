package com.heptafox.niah.sizing;

/** Estimates the token count of a piece of text. */
public interface TokenEstimator {

    int estimate(String text);

    /** Note describing how the estimate is produced, surfaced in catalog/manifest output. */
    String note();
}
