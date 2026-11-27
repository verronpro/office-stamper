/// docx4j 17.x provider for the OfficeStamper API seam. This module is the ONLY
/// place that knows about org.docx4j; the engine depends on officestamper-api, not
/// on docx4j directly.
module pro.verron.officestamper.docx4j17 {
    requires pro.verron.officestamper.utils;
    requires pro.verron.officestamper;

    requires org.docx4j.core;
    // docx4j 17 merged all JAXB object models (wml, dml, pml, sml) into the
    // generated-objects artifact; openxml-objects(-pml/-sml) no longer exist.
    requires org.docx4j.generated_objects;
    // The parent-tracking Child interface moved from org.jvnet.jaxb2_commons.ppp
    // to org.jvnet.jaxb.lang (jaxb-plugins-runtime 4.x, automatic module).
    requires org.jvnet.jaxb.plugins.runtime;
    requires jakarta.xml.bind;
    requires org.jspecify;
    requires org.slf4j;

    exports pro.verron.officestamper.docx;
    exports pro.verron.officestamper.docx.experimental;
    exports pro.verron.officestamper.docx.wml to pro.verron.officestamper.docx4j17.test;
    exports pro.verron.officestamper.docx.openpackaging to pro.verron.officestamper.docx4j17.test;
    exports pro.verron.officestamper.docx.pml to pro.verron.officestamper.docx4j17.test;
    exports pro.verron.officestamper.docx.sml to pro.verron.officestamper.docx4j17.test;
    exports pro.verron.officestamper.docx.preset to pro.verron.officestamper.docx4j17.test;
    exports pro.verron.officestamper.docx.preset.resolvers.image to pro.verron.officestamper.docx4j17.test;
    opens pro.verron.officestamper.docx to org.junit.platform.commons;

    provides pro.verron.officestamper.api.OfficeStamper with pro.verron.officestamper.docx.DocxStamperProvider;
}

