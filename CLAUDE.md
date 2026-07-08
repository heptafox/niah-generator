# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`niah-generator` generates Needle-In-A-Haystack (NIAH) evaluation datasets for LLM benchmarking. It ships **two independent generators**:

1. **Spring Boot app** (Java 21, Spring Boot 4.1.0) — takes a source PDF (Pride and Prejudice, bundled), slices it to size presets, injects counterfactual needles, and exposes them via a REST API and a two-tab static web UI: a **RAG** tab that serves downloadable documents (`.md` / `.docx` / `.pdf`) plus a JSON manifest, and an **Agent Crawl** tab that serves the same haystack as a live HTML page at a URL (`/haystack/{id}`) for agent web-crawling / tool-use evaluation, with a JSON answer key.
2. **`scripts/gen_niah.py`** — a standalone, dependency-free Python script that synthesizes plain-text haystacks with a single needle each and writes them to `dataset/` with an `answer_key.csv`. Unrelated to the Java app; run it directly with `python3 scripts/gen_niah.py` (its `__main__` runs a self-checking `demo()`).

## Commands

```bash
./mvnw clean package              # build
./mvnw spring-boot:run            # run (UI at http://localhost:8080)
./mvnw test                       # all tests
./mvnw test -Dtest=NiahGeneratorApplicationTests   # single test class
./mvnw clean package -DskipTests  # build without tests

python3 scripts/gen_niah.py       # regenerate dataset/ text files + answer_key.csv
```

## Architecture (Java app)

Everything is configured under the `niah.*` keys in `application.yaml`, bound to `NiahProperties` (config package): source PDF path, segment count, token divisor, page/token presets, and the needle pool.

The pipeline is **catalog matrix → on-demand artifact build**, organized by package:

- **`source`** — `HaystackSource` loads and caches the PDF once at startup (`@PostConstruct`): `PdfTextExtractor` (PDFBox) → `TextCleaner` → `CanonicalDocument` (cleaned pages + full text). Downstream code never re-parses the PDF.
- **`sizing`** — `DocumentSlicer` cuts the canonical doc to a `SizePreset` (either first-N-pages or approx-token target). `TokenEstimator` (`CharHeuristicTokenEstimator`) is a chars/divisor heuristic, not a real tokenizer.
- **`catalog`** — `CatalogService` builds the catalog matrix at startup: every `SizePreset` × every `DocFormat`, each an addressable `CatalogEntry` keyed by `id` (`<preset>-<ext>`). `ArtifactBuilder` orchestrates one download: **slice → inject → render → manifest**. `ArtifactCache` memoizes built artifacts; `ManifestWriter` emits the JSON manifest; `ZipPackager` bundles document + manifest when the answer key is requested.
- **`needle`** — `NeedleInjector` places one distinct needle per equal-depth segment, snapped to the nearest sentence boundary; `NeedleMode.ISOLATED` (own paragraph) vs `EMBEDDED` (blended into surrounding prose) share depth and differ only in visibility. Needles come from `NeedlePool` (the configured counterfactual facts). Recorded `charOffset`/`depthPercent` are measured against the delivered body text, excluding format chrome.
- **`render`** — `RendererRegistry` maps `DocFormat` → `DocumentRenderer`: `MarkdownRenderer`, `DocxRenderer` (Apache POI), `PdfRenderer` (OpenPDF), and `HtmlRenderer` (the crawl target — a standalone `noindex` HTML page).
- **`web`** — `CatalogController` serves both UI tabs off the same slice→inject→render→manifest pipeline. RAG tab: `GET /api/catalog` (list), `GET /api/catalog/{id}/download?mode=&answerKey=` (document, or zip with answer-key manifest). Agent Crawl tab: `GET /haystack/{id}?mode=` (haystack as a live HTML page for an agent to crawl) and `GET /api/catalog/{id}/answer-key?mode=` (JSON manifest). `ApiExceptionHandler` maps errors. Static UI in `resources/static` (two tabs, split by `format` client-side: non-`html` → RAG, `html` → crawl).

The source PDF is loaded internally and **never served publicly**.

## Key conventions

- Adding a size variant or needle is config-only (`application.yaml`); needles must number ≥ `segment-count`.
- Token counts everywhere are *approximate heuristics*, surfaced as such in manifests — don't treat them as exact.
- New output format = add a `DocFormat` value + a `DocumentRenderer` and register it; the catalog matrix picks it up automatically.

## License

Apache License 2.0 (see `LICENSE`).
