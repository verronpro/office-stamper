/**
 * This module serves as the main module for the "pro.verron.officestamper" application.
 * It declares the module dependencies and exports the necessary packages.
 * <p>
 * The module requires the following modules:
 * - spring.core
 * - spring.expression
 * - org.docx4j.core
 * <p>
 * It also requires the following modules statically:
 * - org.apache.commons.io
 * - org.slf4j
 * - jakarta.xml.bind
 * <p>
 * The module opens the following packages for reflection and runtime access:
 * - pro.verron.officestamper.api
 * - pro.verron.officestamper.preset
 * <p>
 * The module exports the following packages for use by other modules:
 * - pro.verron.officestamper.api
 * - pro.verron.officestamper.preset
 * <p>
 * Additionally, it opens the "pro.verron.officestamper.core" package to the "pro.verron.officestamper.test" module,
 * and exports it for use by the same module.
 * <p>
 * WARNING: The module also opens and exports packages that should be innacessible in the next version.
 * These packages are:
 * - pro.verron.officestamper
 * - org.wickedsource.docxstamper
 * - org.wickedsource.docxstamper.api
 * - org.wickedsource.docxstamper.api.commentprocessor
 * - org.wickedsource.docxstamper.el
 * - org.wickedsource.docxstamper.util
 * - org.wickedsource.docxstamper.processor
 * - org.wickedsource.docxstamper.processor.table
 */
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

    opens pro.verron.officestamper.core to pro.verron.officestamper.test;
    exports pro.verron.officestamper.core to pro.verron.officestamper.test;

    // TODO_LATER: remove all the following exports in next version
    opens pro.verron.officestamper;
    exports pro.verron.officestamper;
    exports org.wickedsource.docxstamper;
    exports org.wickedsource.docxstamper.api;
    exports org.wickedsource.docxstamper.el;
    exports org.wickedsource.docxstamper.util;
    exports org.wickedsource.docxstamper.processor;
    exports org.wickedsource.docxstamper.processor.table;
}
