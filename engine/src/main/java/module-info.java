/// The core template-stamping engine for OfficeStamper.
///
/// This module provides the main DOCX stamping functionality, processing Spring
/// Expression Language (SpEL) expressions embedded in Word templates and
/// replacing them with values from a context object. It supports
/// AnchoredComment-based directives for conditional display, repetition, and content
/// replacement, along with a configurable pipeline of pre-processors,
/// AnchoredComment processors, resolvers, and post-processors.
///
/// ## Exported Packages
/// - [pro.verron.officestamper.api] - Core API interfaces and types for
/// document stamping
/// - [pro.verron.officestamper.preset] - Pre-configured stampers,
/// configurations, and ready-made processors and resolvers
module pro.verron.officestamper {
    requires spring.core;
    requires spring.expression;
    uses javax.imageio.spi.ImageReaderSpi;

    requires transitive org.docx4j.core;

    requires static org.apache.commons.io;
    requires static org.slf4j;
    requires static jakarta.xml.bind;
    requires org.docx4j.openxml_objects;
    requires org.jspecify;
    requires pro.verron.officestamper.api;
    requires pro.verron.officestamper.utils;

    opens pro.verron.officestamper.api;
    exports pro.verron.officestamper.api;

    opens pro.verron.officestamper.preset;
    exports pro.verron.officestamper.preset;

    exports pro.verron.officestamper.core to pro.verron.officestamper.test, pro.verron.officestamper.docx4j11;
}

