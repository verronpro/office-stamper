/// Docx-free low-level utility classes shared across the OfficeStamper modules.
///
/// This module holds only utilities that do NOT depend on any OOXML library:
/// functional programming helpers, iterator implementations and SVG processing.
/// All image and OOXML-coupled helpers (WML, PML, SML, Open Packaging) live in
/// the version-specific provider modules, since their signatures are tied to a
/// concrete docx4j release.
///
/// ## Exported Packages
/// - [pro.verron.officestamper.utils.function] - Functional programming
/// helpers
/// - [pro.verron.officestamper.utils.iterator] - Iterator implementations and
/// utilities
/// - [pro.verron.officestamper.utils.svg] - SVG processing utilities
module pro.verron.officestamper.utils {
    requires org.jspecify;
    requires java.desktop;

    exports pro.verron.officestamper.utils;
    exports pro.verron.officestamper.utils.iterator;
    exports pro.verron.officestamper.utils.function;
    exports pro.verron.officestamper.utils.svg;
}
