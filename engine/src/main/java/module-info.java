/// This module serves as the main module for the "pro.verron.officestamper" application.
/// It declares the module dependencies and exports the necessary packages.
///
/// The module requires the following modules:
/// - spring.core
/// - spring.expression
/// - org.docx4j.core
///
/// It also requires the following modules statically:
/// - org.apache.commons.io
/// - org.slf4j
/// - jakarta.xml.bind
///
/// The module opens the following packages for reflection and runtime access:
/// - pro.verron.officestamper.api
/// - pro.verron.officestamper.preset
///
/// The module exports the following packages for use by other modules:
/// - pro.verron.officestamper.api
/// - pro.verron.officestamper.preset
///
/// Additionally, it opens the "pro.verron.officestamper.core" package to the "pro.verron.officestamper.test" module,
/// and exports it for use by the same module.
module pro.verron.officestamper {
    requires spring.core;
    requires spring.expression;

    requires transitive org.docx4j.core;

    requires static org.apache.commons.io;
    requires static org.slf4j;
    requires static jakarta.xml.bind;

    opens pro.verron.officestamper.api;
    exports pro.verron.officestamper.api;

    opens pro.verron.officestamper.preset;
    exports pro.verron.officestamper.preset;

    opens pro.verron.officestamper.experimental to pro.verron.officestamper.test;
    exports pro.verron.officestamper.experimental to pro.verron.officestamper.test;
    exports pro.verron.officestamper.utils;
    opens pro.verron.officestamper.utils;
}
