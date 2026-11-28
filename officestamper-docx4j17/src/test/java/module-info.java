/// Test suite for the OfficeStamper docx4j 11 provider module.
///
/// Contains integration tests, architecture tests, and shared test utilities
/// validating the full docx pipeline (docx4j-free engine + docx4j-backed
/// provider processors).
module pro.verron.officestamper.docx4j17.test {
    requires pro.verron.officestamper;
    requires pro.verron.officestamper.docx4j17;
    requires org.objectweb.asm;
    uses javax.imageio.spi.ImageReaderSpi;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires spring.core;
    requires spring.context;
    requires spring.expression;

    requires org.docx4j.generated_objects;
    requires org.docx4j.core;

    requires org.slf4j;
    requires jakarta.xml.bind;
    requires com.tngtech.archunit.junit5.api;
    requires com.tngtech.archunit;
    requires pro.verron.asciidoc.core;
    requires pro.verron.asciidoc.docx17;
    requires pro.verron.officestamper.utils;

    opens pro.verron.officestamper.docx4j17.test;
    exports pro.verron.officestamper.docx4j17.test;
    exports pro.verron.officestamper.docx4j17.test.architecture;
    opens pro.verron.officestamper.docx4j17.test.architecture;
    exports pro.verron.officestamper.test;
    opens pro.verron.officestamper.test;
    exports pro.verron.officestamper.test.utils;
    opens pro.verron.officestamper.test.utils;
    exports pro.verron.officestamper.test.experimental;
    opens pro.verron.officestamper.test.experimental;
    uses pro.verron.officestamper.test.TestFlavor;
    provides pro.verron.officestamper.test.TestFlavor with pro.verron.officestamper.docx4j17.test.TestFlavorImpl;
}
