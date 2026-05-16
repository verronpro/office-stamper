/// The [pro.verron.officestamper.asciidoc] module.
///
/// This module provides utilities to turn AsciiDoc content into WordprocessingML (DOCX) fragments or documents using
/// docx4j. It focuses on converting rich-text into objects consumable by
/// Office‑stamper’s engine or by docx4j directly.
///
///
/// Exports:
///
///     - [pro.verron.officestamper.asciidoc] – public API for AsciiDoc to DOCX conversion.
///
///
module pro.verron.officestamper.asciidoc {
    requires javafx.controls;
    requires javafx.graphics;
    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires jakarta.xml.bind;
    requires org.jspecify;
    requires org.slf4j;

    opens pro.verron.officestamper.asciidoc;
    exports pro.verron.officestamper.asciidoc;
}
