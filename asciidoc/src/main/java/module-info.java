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
    requires org.apache.xmlgraphics.batik.transcoder;
    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;

    requires jakarta.xml.bind;
    requires org.jspecify;
    requires org.slf4j;
    requires org.objectweb.asm;
    requires org.asciidoctor.asciidoctorj;
    requires org.asciidoctor.asciidoctorj.api;

    opens pro.verron.officestamper.asciidoc;
    exports pro.verron.officestamper.asciidoc;

    provides org.asciidoctor.jruby.extension.spi.ExtensionRegistry
            with pro.verron.officestamper.asciidoc.AsciiDocPreviewExtensionRegistry;
}
