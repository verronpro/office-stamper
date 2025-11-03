/// # Office-stamper – A powerful Java template engine for dynamic DOCX document generation.
///
/// Office Stamper lets developers create dynamic DOCX documents at runtime using templates
/// designed in any Word processor.
/// The library uses Spring Expression Language (SpEL) for powerful template expressions, preserves
///  all formatting from the original template, and offers extensive customization options.
///
/// ## Key Features:
///     - Expression-based templating using Spring Expression Language (SpEL)
///     - Comment-based processing for special document instructions
///     - Complete preservation of original template formatting
///     - Extensible with custom functions
///     - Type-safe Java integration
///     - Flexible configuration options
///
/// ## Quick Start Example:
/// <pre>
/// var context = new YourPojoContext(param1, param2, param3);
/// var stamper = OfficeStampers.docxStamper();
/// var templatePath = Paths.get("template.docx");
/// var outputPath = Paths.get("output.docx");
/// try(
///     var template = Files.newInputStream(templatePath);
///     var output = Files.newOutputStream(outputPath);
/// ) {
///     stamper.stamp(template, context, output);
/// }
/// </pre>
///
/// This module definition declares the `pro.verron.officestamper` module and configures
/// its dependencies, exports, and encapsulated packages.
/// Module Dependencies:
/// - `spring.core`: used for core Spring framework features.
/// - `spring.expression`: used for supporting Spring expression language functionality.
/// - `org.docx4j.core`: declared as transitive, indicating that dependent modules also require this dependency.
/// - `org.apache.commons.io`: declared as a static dependency, meaning it is optional and only used during
/// compile-time.
/// - `org.slf4j`: declared as a static dependency for logging purposes, optional.
/// - `jakarta.xml.bind`: declared as a static dependency for XML binding, optional.
/// Packages and Access Control:
/// - `pro.verron.officestamper.api`:
///   - Exports the `api` package to all modules.
///   - Opens the `api` package for reflection.
/// - `pro.verron.officestamper.preset`:
///   - Exports the `preset` package to all modules.
///   - Opens the `preset` package for reflection.
/// - `pro.verron.officestamper.experimental`:
///   - Opens the `experimental` package for reflection to the `pro.verron.officestamper.test` module.
///   - Exports the `experimental` package only to the `pro.verron.officestamper.test` module.
/// - `pro.verron.officestamper.utils`:
///   - Exports the `utils` package to all modules.
///   - Opens the `utils` package for reflection.
module pro.verron.officestamper {
    requires spring.core;
    requires spring.expression;

    requires transitive org.docx4j.core;

    requires static org.apache.commons.io;
    requires static org.slf4j;
    requires static jakarta.xml.bind;
    requires org.jetbrains.annotations;

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
