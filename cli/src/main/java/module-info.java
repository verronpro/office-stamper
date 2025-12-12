/// The `pro.verron.officestamper.cli` module.
///
/// Command Line Interface for Office‑stamper. It lets you stamp DOCX or PPTX templates from various input formats (CSV,
/// Properties, XML/HTML, JSON, XLSX) directly from the terminal. This module depends on the core engine module
/// `pro.verron.officestamper` and bundles parsing utilities (Jackson, OpenCSV).
module pro.verron.officestamper.cli {
    requires pro.verron.officestamper; // engine

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

    // Picocli uses reflection to populate fields on the command class
    opens pro.verron.officestamper to info.picocli;
}
