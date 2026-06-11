/// Command-line interface for OfficeStamper.
///
/// This module provides a CLI for stamping DOCX or PPTX templates from the
/// terminal. It supports multiple input formats (CSV, Properties, XML/HTML,
/// JSON, YAML, XLSX) as data sources, watch mode for automatic re-stamping
/// on file changes, and dry-run validation. Built on Picocli, it depends on the
/// core engine and excel-context modules.
module pro.verron.officestamper.cli {
    requires pro.verron.officestamper;
    requires pro.verron.officestamper.excel;
    requires pro.verron.officestamper.asciidoc.core;
    requires pro.verron.officestamper.asciidoc.compiler;

    requires org.slf4j;
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
