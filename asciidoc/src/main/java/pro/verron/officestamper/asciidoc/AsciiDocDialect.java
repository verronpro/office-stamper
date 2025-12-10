package pro.verron.officestamper.asciidoc;

/// Output dialects for DOCX → AsciiDoc conversion.
///
/// COMPAT aims at preserving the legacy Stringifier output (used by existing tests). ADOC emits a cleaner, more
/// idiomatic AsciiDoc representation.
public enum AsciiDocDialect {
    COMPAT,
    ADOC
}
