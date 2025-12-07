/// The module descriptor for the `pro.verron.officestamper` module.
///
/// This module provides functionalities related to office document manipulation
/// and stamping, leveraging libraries like docx4j and Spring Framework.
///
/// Requirements:
/// - Requires `spring.core` and `spring.expression` for using Spring Framework functionalities.
/// - Requires `org.docx4j.core` transitively for handling office document processing.
/// - Optionally requires `org.apache.commons.io`, `org.slf4j`, and `jakarta.xml.bind` as static dependencies.
/// - Requires `org.jetbrains.annotations` for annotation support.
/// - Requires `org.docx4j.openxml_objects` for OpenXML document handling.
///
/// Module Exports:
/// - Exports `pro.verron.officestamper.api` for public API access.
/// - Exports `pro.verron.officestamper.preset` for predefined document processing utilities.
/// - Exports `pro.verron.officestamper.experimental` and `pro.verron.officestamper.preset.preprocessors.placeholders`
///   to `pro.verron.officestamper.test` for experimental features and testing purposes.
/// - Exports `pro.verron.officestamper.utils` for utility functionalities.
///
/// Opens:
/// - Opens `pro.verron.officestamper.api` and `pro.verron.officestamper.preset` for reflective access.
/// - Opens `pro.verron.officestamper.experimental` specifically to `pro.verron.officestamper.test`.
/// - Opens `pro.verron.officestamper.utils` for reflective access.
module pro.verron.officestamper {
    requires spring.core;
    requires spring.expression;

    requires transitive org.docx4j.core;

    requires static org.apache.commons.io;
    requires static org.slf4j;
    requires static jakarta.xml.bind;
    requires org.jetbrains.annotations;
    requires org.docx4j.openxml_objects;
    requires org.jspecify;

    opens pro.verron.officestamper.api;
    exports pro.verron.officestamper.api;

    opens pro.verron.officestamper.preset;
    exports pro.verron.officestamper.preset;

    opens pro.verron.officestamper.experimental to pro.verron.officestamper.test;
    exports pro.verron.officestamper.experimental to pro.verron.officestamper.test;
    exports pro.verron.officestamper.preset.preprocessors.placeholders to pro.verron.officestamper.test;
    exports pro.verron.officestamper.utils;
    opens pro.verron.officestamper.utils;
}
