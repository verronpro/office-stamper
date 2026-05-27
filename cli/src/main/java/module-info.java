/// The [pro.verron.officestamper] CLI module.
///
/// Command Line Interface for Office‑stamper. It lets you stamp DOCX or PPTX templates from various input formats (CSV,
/// Properties, XML/HTML, JSON, XLSX) directly from the terminal. This module depends on the core engine module
/// [pro.verron.officestamper] and bundles parsing utilities (Jackson, OpenCSV).
module pro.verron.officestamper.cli {
    requires pro.verron.officestamper;
    requires pro.verron.officestamper.excel;
    requires pro.verron.officestamper.asciidoc.core;
    requires pro.verron.officestamper.asciidoc.compiler;

    requires java.logging;
    requires java.xml;
    requires java.prefs;

    requires info.picocli;
    requires com.opencsv;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires org.jspecify;

    // Picocli uses reflection to populate fields in the command class
    opens pro.verron.officestamper to info.picocli;
    exports pro.verron.officestamper to com.fasterxml.jackson.databind;
}
