package com.heptafox.niah.source;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Turns the raw per-line text PDFBox emits for a page into clean, paragraph-delimited prose:
 * strips the Planet eBook chrome (running footers, page numbers, cover promo, running title),
 * de-hyphenates words split across line breaks, and reflows wrapped lines back into paragraphs.
 *
 * <p>The chrome patterns are specific to this Planet eBook edition of <em>Pride and Prejudice</em>.
 */
@Component
public class TextCleaner {

    /** Recurring footer ("Free eBooks at Planet eBook.com"), with any glued page number on either side. */
    private static final Pattern FOOTER =
            Pattern.compile("(?i)\\d{0,4}\\s*free\\s+ebooks?\\s+at\\s+planet\\s*ebook\\.com\\s*\\d{0,4}");
    /** Cover promo sentences. */
    private static final Pattern PROMO_DOWNLOAD = Pattern.compile("(?i)download\\s+free\\s+ebooks[^.]*\\.");
    private static final Pattern PROMO_SUBSCRIBE = Pattern.compile("(?i)subscribe\\s+to\\s+our\\s+free\\s+ebooks[^.]*\\.");
    /** Running title, which PDFBox often glues to the page body (e.g. "Pride and Prejudice Chapter 1 ..."). */
    private static final Pattern RUNNING_TITLE = Pattern.compile("(?i)\\bpride\\s+and\\s+prejudice\\b");
    /** A line that is nothing but a page number. */
    private static final Pattern PAGE_NUMBER_ONLY = Pattern.compile("^\\s*\\d{1,4}\\s*$");
    /** Opening quotation marks that signal the start of a new (dialogue) paragraph. */
    private static final Pattern OPENS_QUOTE = Pattern.compile("^[\"'‘’“”]");

    /** Cleans one extracted page into paragraphs separated by blank lines. */
    public String cleanPage(String rawPage) {
        String dechromed = RUNNING_TITLE.matcher(
                PROMO_SUBSCRIBE.matcher(
                        PROMO_DOWNLOAD.matcher(
                                FOOTER.matcher(rawPage).replaceAll(" "))
                                .replaceAll(" "))
                        .replaceAll(" "))
                .replaceAll(" ");

        List<String> kept = new ArrayList<>();
        for (String line : dechromed.split("\\R")) {
            String trimmed = line.strip().replaceAll(" {2,}", " ");
            if (trimmed.isEmpty() || PAGE_NUMBER_ONLY.matcher(trimmed).matches()) {
                continue;
            }
            kept.add(trimmed);
        }
        return reflow(kept);
    }

    /** Joins wrapped lines into paragraphs, de-hyphenating across breaks. */
    private String reflow(List<String> lines) {
        List<String> paragraphs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String previous = "";

        for (String line : lines) {
            if (current.length() > 0 && startsNewParagraph(previous, line)) {
                paragraphs.add(current.toString());
                current.setLength(0);
            }
            appendLine(current, line);
            previous = line;
        }
        if (current.length() > 0) {
            paragraphs.add(current.toString());
        }
        return String.join("\n\n", paragraphs);
    }

    private void appendLine(StringBuilder current, String line) {
        if (current.length() == 0) {
            current.append(line);
            return;
        }
        int end = current.length();
        if (current.charAt(end - 1) == '-') {
            // de-hyphenate: drop the trailing hyphen and join directly (e.g. "extraordi-" + "nary").
            current.setLength(end - 1);
            current.append(line);
        } else {
            current.append(' ').append(line);
        }
    }

    /**
     * Heuristic paragraph boundary: a new dialogue line, or a previous line that ended a sentence
     * well short of the right margin (a typical paragraph-final line).
     */
    private boolean startsNewParagraph(String previous, String line) {
        if (OPENS_QUOTE.matcher(line).find()) {
            return true;
        }
        boolean prevEndsSentence = previous.endsWith(".") || previous.endsWith("!")
                || previous.endsWith("?") || previous.endsWith("\"") || previous.endsWith("”");
        return prevEndsSentence && previous.length() < 55;
    }
}
