/// The `pro.verron.officestamper.asciidoc` module.
///
/// This module provides utilities to turn AsciiDoc content into WordprocessingML (DOCX) fragments or documents using
/// AsciidoctorJ and docx4j. It focuses on converting rich-text produced by Asciidoctor into objects consumable by
/// Office‑stamper’s engine or by docx4j directly.
///
///
/// Exports:
///
///     - `pro.verron.officestamper.asciidoc` – public API for AsciiDoc to DOCX conversion.
///
///
module pro.verron.officestamper.asciidoc {
    requires javafx.controls;
    requires javafx.graphics;
    requires org.asciidoctor.asciidoctorj.api;
    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires jakarta.xml.bind;
    requires org.jspecify;

    opens pro.verron.officestamper.asciidoc;
    exports pro.verron.officestamper.asciidoc;
}
