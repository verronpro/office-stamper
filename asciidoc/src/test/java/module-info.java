/// This module is designed for testing purposes within the Office Stamper Asciidoc library. Module dependencies:
/// - Requires JUnit Jupiter API for testing functionalities.
/// - Requires the Office Stamper Asciidoc module to test its features. Module functionalities:
/// - Exports the test package to make it accessible to other modules.
/// - Opens the test package for deep reflection by other modules.
module pro.verron.officestamper.asciidoc.test {
    requires org.junit.jupiter.api;
    requires pro.verron.officestamper.asciidoc;
    requires org.junit.jupiter.params;
    exports pro.verron.officestamper.asciidoc.test;
    opens pro.verron.officestamper.asciidoc.test;
}
