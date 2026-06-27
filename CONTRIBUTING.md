# Contributing

Thanks for your interest in improving niah-generator!

## Getting started

```bash
git clone https://github.com/heptafox/niah-generator.git
cd niah-generator
./mvnw clean package      # build + run tests
./mvnw spring-boot:run    # UI at http://localhost:8080
```

## Pull requests

1. Fork and branch off `main`.
2. Make your change. Add or update a test for any non-trivial logic.
3. Run `./mvnw test` and make sure it passes.
4. Open a PR with a clear description of what and why.

## Conventions

- Size variants and needles are config-only — edit `niah.*` in `application.yaml`.
- New output format = add a `DocFormat` value + a `DocumentRenderer`, then register it.
- Token counts are approximate heuristics; keep them labelled as such.

See [CLAUDE.md](CLAUDE.md) for a full architecture overview.
