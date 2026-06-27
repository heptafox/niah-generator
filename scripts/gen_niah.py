#!/usr/bin/env python3
"""Generate a Needle-In-A-Haystack eval dataset as separate files (no zip).

Output: dataset/niah_<tokens>_<depth>.txt  +  dataset/answer_key.csv
Token counts are approximate (word-based, ~1.3 tokens/word).
"""
import csv
import os

OUT = os.path.join(os.path.dirname(__file__), "..", "dataset")
TOK_PER_WORD = 1.3
LENGTHS = [1000, 4000, 8000, 16000]   # target tokens
DEPTHS = [0.10, 0.50, 0.90]            # needle position in haystack

# Varied filler so the needle isn't trivially spotted in pure repetition.
SUBJECTS = ["The committee", "A traveler", "The engineer", "Each season", "The old library",
            "A distant signal", "The morning crew", "Every map", "The river", "A quiet observer"]
VERBS = ["recorded", "examined", "rearranged", "questioned", "preserved",
         "measured", "ignored", "celebrated", "redrew", "forgot"]
OBJECTS = ["the weathered ledgers", "a column of figures", "the harbor lights", "several old routes",
           "the founding charter", "an unmarked crate", "the tide tables", "a faded photograph",
           "the council minutes", "three brass keys"]
TAILS = ["before the storm arrived.", "without explanation.", "as the lamps were lit.",
         "in the usual careful manner.", "long after the others had left.", "for reasons unknown.",
         "while the bells rang.", "according to old custom.", "under a grey sky.", "and said nothing more."]

# Needle facts: (city, passcode)
NEEDLES = [
    ("Lisbon", "4471"), ("Nairobi", "8830"), ("Reykjavik", "1925"),
    ("Montevideo", "6042"), ("Hanoi", "7318"), ("Tbilisi", "5096"),
    ("Wellington", "2287"), ("Kraków", "9613"), ("Bogotá", "3754"),
    ("Helsinki", "1408"), ("Marrakesh", "6671"), ("Osaka", "8059"),
]


def sentence(i):
    return (f"{SUBJECTS[i % len(SUBJECTS)]} {VERBS[(i // 3) % len(VERBS)]} "
            f"{OBJECTS[(i // 7) % len(OBJECTS)]} {TAILS[(i // 5) % len(TAILS)]}")


def haystack(target_tokens):
    target_words = int(target_tokens / TOK_PER_WORD)
    sents, words = [], 0
    i = 0
    while words < target_words:
        s = sentence(i)
        sents.append(s)
        words += len(s.split())
        i += 1
    return sents


def build():
    os.makedirs(OUT, exist_ok=True)
    key_rows = []
    for n, tokens in enumerate(LENGTHS):
        for depth in DEPTHS:
            city, code = NEEDLES[(n * len(DEPTHS) + DEPTHS.index(depth)) % len(NEEDLES)]
            needle = f"The secret passcode for {city} is {code}."
            sents = haystack(tokens)
            pos = max(1, min(len(sents) - 1, int(len(sents) * depth)))
            sents.insert(pos, needle)
            text = (
                "Read the passage below and answer the question at the end.\n\n"
                + " ".join(sents)
                + f"\n\nQuestion: What is the secret passcode for {city}?\n"
            )
            depth_pct = int(depth * 100)
            fname = f"niah_{tokens}_{depth_pct}.txt"
            with open(os.path.join(OUT, fname), "w") as f:
                f.write(text)
            key_rows.append({
                "file": fname,
                "approx_tokens": tokens,
                "needle_depth_pct": depth_pct,
                "question": f"What is the secret passcode for {city}?",
                "answer": code,
                "needle": needle,
            })
    with open(os.path.join(OUT, "answer_key.csv"), "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(key_rows[0].keys()))
        w.writeheader()
        w.writerows(key_rows)
    return key_rows


def demo():
    """Self-check: needle is present exactly once and answer matches the key."""
    rows = build()
    for r in rows:
        with open(os.path.join(OUT, r["file"])) as f:
            text = f.read()
        assert text.count(r["needle"]) == 1, f"needle not unique in {r['file']}"
        assert r["answer"] in r["needle"], "answer/needle mismatch"
        words = len(text.split())
        # within 25% of target word count
        assert abs(words - r["approx_tokens"] / TOK_PER_WORD) < r["approx_tokens"] * 0.25, \
            f"length off in {r['file']}: {words} words"
    print(f"OK: {len(rows)} samples + answer_key.csv in {os.path.relpath(OUT)}")


if __name__ == "__main__":
    demo()
