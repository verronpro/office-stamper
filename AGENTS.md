# Office-stamper — AGENTS.md

Agent-agnostic guidance for working in the **office-stamper** project. This file is the standalone
guidance for this project; it does not assume any relationship to other projects.

## Project layout

Maven modules:

- `engine` — public API and core processing. Entry points: `OfficeStampers` (factory) and
  `OfficeStamperConfigurations` (presets `minimal()`, `standard()`, `full()`).
- `officestamper-docx4j11` / `officestamper-docx4j17` — docx4j providers (engine + matching
  docx4j transitively); the engine itself is docx-free.
- `officestamper-test-shared` — shared docx-stamping test sources, compiled into both
  provider test modules and run once per flavor (see Testing).
- `utils` — low-level utilities with no engine dependency.
- `cli` — command-line interface (Picocli).
- `excel-context` — Excel/XLSX context support.

The engine processes DOCX templates via docx4j, evaluates `${...}` placeholders with Spring
Expression Language, and uses comment-based processors (`repeat`, `displayIf`, `replaceWith`).
Security defaults to **restricted** SpEL/SVG parsing; opt into `SecurityMode.PERMISSIVE` only for
trusted templates.

## General development standards

This project uses **Java 25** and **Maven**.

### Build
- `mvn clean install` — build the project.
- `mvn clean install -DskipTests` — build without running tests.
- `mvn clean install -pl <module>` — build a single Maven module within this project.
- `mvn clean install -pl <module> -am` — build a module and its dependencies.
- `mvn site` — generate the Maven documentation site from `src/site/asciidoc/`.
- `mvn deploy -Pgpg` — deploy to Maven Central (gpg profile).

### Testing
- JUnit 5 (Jupiter); use `@DisplayName` for descriptive names; follow Arrange-Act-Assert.
- Tests live in `src/test/java/`; template `.docx` fixtures live in
  `engine/src/test/resources/` (or a `sources` subfolder).
- **Shared docx-stamping tests** live in `officestamper-test-shared/src/test/java`
  (package `pro.verron.officestamper.test`) and are compiled into BOTH provider test
  modules via `build-helper-maven-plugin add-test-source`, so each suite runs once per
  docx4j flavor. Author new docx-stamping tests THERE, once — never mirror them between
  `officestamper-docx4j11` and `officestamper-docx4j17`. Flavor differences go through the
  `TestFlavor` seam (`Flavor.get()`), implemented once per provider in its
  `TestFlavorImpl`; only provider-internal tests (shims, discovery, wml iterator,
  ArchUnit rules) stay in the provider modules.
- Coverage via JaCoCo, mutation testing via Pitest, architecture constraints via ArchUnit.
- `java.awt.headless=true` is set automatically by the Surefire plugin.

### Code style
- Java 25 features in use: records, JPMS modules (`module-info.java`), modern APIs.
- Soft line-length limit: 120 characters. Indentation: 4 spaces.
- Opening braces on the same line; `else` on a new line.
- Naming: PascalCase for classes/interfaces, camelCase for methods/variables,
  UPPER_SNAKE_CASE for constants.
- Prefer composition to inheritance; static factory methods or builders for complex objects;
  constructor injection for dependencies.
- Javadoc required for all public elements; use Markdown syntax inside Javadoc.
- Custom exceptions (e.g. `OfficeStamperException`); do not swallow exceptions; use
  try-with-resources.
- IntelliJ code style file: `intellij-style.xml` at this project root.

## Rules

- Modules must **never** depend on test artifacts (e.g. `test-jar`) from other modules. Move
  shared test utilities into the main source set of an appropriate internal module (e.g. `utils`).
- Keep `intellij-style.xml` in sync if the style changes.
