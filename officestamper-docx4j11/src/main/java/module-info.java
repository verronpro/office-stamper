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
    exports pro.verron.officestamper.docx4j11.wml to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.openpackaging to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.pml to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.sml to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.image to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.preset to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.preset.prooferror to pro.verron.officestamper.docx4j11.test;
    exports pro.verron.officestamper.docx4j11.preset.resolvers.image to pro.verron.officestamper.docx4j11.test;
    opens pro.verron.officestamper.docx4j11 to org.junit.platform.commons;

    // Compatibility shims (deprecated): the pre-3.9 package locations, so code
    // written against the single-artifact 3.3.x line keeps compiling. Every shim
    // extends the corresponding docx4j11.* class above.
    exports pro.verron.officestamper.utils.wml;
    exports pro.verron.officestamper.utils.openpackaging;
    exports pro.verron.officestamper.utils.image;
    exports pro.verron.officestamper.utils.pml;
    exports pro.verron.officestamper.utils.sml;
    exports pro.verron.officestamper.experimental;
    exports pro.verron.officestamper.preset.preprocessors.malformedcomments;
    exports pro.verron.officestamper.preset.preprocessors.prooferror;
    exports pro.verron.officestamper.preset.preprocessors.rmlang;
    exports pro.verron.officestamper.preset.preprocessors.similarrun;
    exports pro.verron.officestamper.preset.processors.repeat;
    exports pro.verron.officestamper.preset.postprocessors;
    exports pro.verron.officestamper.preset.postprocessors.cleanendnotes;
    exports pro.verron.officestamper.preset.postprocessors.cleanfootnotes;
    exports pro.verron.officestamper.preset.resolvers.image;

    provides pro.verron.officestamper.spi.OfficeStamper with pro.verron.officestamper.docx4j11.DocxStamperProvider;
}

