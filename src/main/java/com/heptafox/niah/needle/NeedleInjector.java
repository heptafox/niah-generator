package com.heptafox.niah.needle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.heptafox.niah.config.NiahProperties;

/**
 * Injects one distinct needle per equal-depth segment. Each needle is placed at the center of its
 * segment ((i + 0.5)/n, e.g. 17/50/83% for three segments), snapped to the nearest sentence boundary
 * so the needle stays an intact, answerable sentence and the recorded depth stays accurate.
 *
 * <p>{@link NeedleMode#ISOLATED} breaks the needle out as its own paragraph; {@link NeedleMode#EMBEDDED}
 * blends it into the surrounding paragraph. Both land at the same boundary, so they share a depth and
 * differ only in visibility.
 */
@Component
public class NeedleInjector {

    private final NeedlePool pool;
    private final int segmentCount;

    public NeedleInjector(NeedlePool pool, NiahProperties properties) {
        this.pool = pool;
        this.segmentCount = properties.segmentCount();
    }

    public InjectionResult inject(String haystack, NeedleMode mode) {
        List<Integer> boundaries = sentenceBoundaries(haystack);
        int total = Math.max(haystack.length(), 1);

        // Choose a distinct boundary nearest each segment's exact target depth.
        int[] positionForSegment = new int[segmentCount];
        Set<Integer> used = new HashSet<>();
        for (int seg = 0; seg < segmentCount; seg++) {
            int target = (int) Math.round((seg + 0.5) / segmentCount * total);
            positionForSegment[seg] = nearestUnusedBoundary(boundaries, target, haystack.length(), used);
            used.add(positionForSegment[seg]);
        }

        // Insert highest-offset first so earlier positions stay valid.
        StringBuilder sb = new StringBuilder(haystack);
        List<Integer> order = new ArrayList<>(used);
        order.sort((a, b) -> Integer.compare(b, a));
        for (int pos : order) {
            int seg = segmentAt(positionForSegment, pos);
            String text = pool.forSegment(seg).text();
            sb.insert(pos, switch (mode) {
                case ISOLATED -> "\n\n" + text + "\n\n";
                case EMBEDDED -> " " + text;
            });
        }

        List<String> paragraphs = new ArrayList<>();
        for (String para : sb.toString().split("\n\n")) {
            String trimmed = para.strip();
            if (!trimmed.isEmpty()) {
                paragraphs.add(trimmed);
            }
        }

        // Record positions against the delivered body (paragraphs joined by blank lines).
        String body = String.join("\n\n", paragraphs);
        int bodyLen = Math.max(body.length(), 1);
        List<InjectedNeedle> injected = new ArrayList<>();
        for (int seg = 0; seg < segmentCount; seg++) {
            Needle needle = pool.forSegment(seg);
            int offset = body.indexOf(needle.text());
            int depthPercent = (int) Math.round(100.0 * offset / bodyLen);
            injected.add(new InjectedNeedle(needle, seg, depthPercent, offset));
        }
        return new InjectionResult(paragraphs, injected);
    }

    /** Insertion points: the whitespace right after a sentence-ending mark (and any closing quotes). */
    private List<Integer> sentenceBoundaries(String text) {
        List<Integer> boundaries = new ArrayList<>();
        for (int i = 0; i < text.length() - 1; i++) {
            char c = text.charAt(i);
            if (c != '.' && c != '!' && c != '?') {
                continue;
            }
            int j = i + 1;
            while (j < text.length() && isClosingQuote(text.charAt(j))) {
                j++;
            }
            if (j < text.length() && Character.isWhitespace(text.charAt(j))) {
                boundaries.add(j);
            }
        }
        return boundaries;
    }

    private boolean isClosingQuote(char c) {
        return c == '"' || c == '\'' || c == '”' || c == '’';
    }

    private int nearestUnusedBoundary(List<Integer> boundaries, int target, int length, Set<Integer> used) {
        int best = -1;
        int bestDistance = Integer.MAX_VALUE;
        for (int boundary : boundaries) {
            if (used.contains(boundary)) {
                continue;
            }
            int distance = Math.abs(boundary - target);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = boundary;
            }
        }
        // Fallback for tiny inputs with no spare boundary: clamp the raw target.
        return best >= 0 ? best : Math.min(Math.max(target, 0), length);
    }

    private int segmentAt(int[] positionForSegment, int position) {
        for (int seg = 0; seg < positionForSegment.length; seg++) {
            if (positionForSegment[seg] == position) {
                return seg;
            }
        }
        return 0;
    }
}
