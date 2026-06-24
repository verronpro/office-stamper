# Office-stamper Specific Guidelines

This document contains guidelines specific to the Office-stamper engine. For general Java coding standards, naming
conventions, and testing principles, refer to the root [.junie/AGENTS.md](../../.junie/AGENTS.md).

## Project-Specific Details

### Dependencies

- Modules should never depend on test artifacts (e.g., `test-jar`) from other modules. Shared test utilities should be
  moved to the main source set of an appropriate module (e.g., `asciidoc` or `utils`).
