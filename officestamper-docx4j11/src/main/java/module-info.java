/// docx4j 11.x provider for the OfficeStamper API seam. This module is the ONLY
/// place that knows about org.docx4j; the engine depends on officestamper-api, not
/// on docx4j directly.
module pro.verron.officestamper.docx4j11 {
    requires pro.verron.officestamper.api;
    requires pro.verron.officestamper.utils;
    requires pro.verron.officestamper;

    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires org.docx4j.openxml_objects_pml;
    requires org.docx4j.openxml_objects_sml;
    requires jakarta.xml.bind;
    requires org.jspecify;
    requires org.slf4j;

    exports pro.verron.officestamper.docx4j11;
    exports pro.verron.officestamper.docx4j11.experimental;
    exports pro.verron.officestamper.docx4j11.preset to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.preset.resolvers.image to pro.verron.officestamper.docx4j11.test;
    opens pro.verron.officestamper.docx4j11 to org.junit.platform.commons;

    provides pro.verron.officestamper.spi.OfficeStamper with pro.verron.officestamper.docx4j11.DocxStamperProvider;
}

