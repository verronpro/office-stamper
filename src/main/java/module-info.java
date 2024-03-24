module pro.verron.officestamper {
    requires spring.core;
    requires spring.expression;

    requires transitive org.docx4j.core;

    requires static org.apache.commons.io;
    requires static org.slf4j;
    requires static jakarta.xml.bind;

    opens pro.verron.docxstamper.api;
    opens pro.verron.docxstamper.preset;

    opens pro.verron.docxstamper.core to pro.verron.officestamper.test;
    exports pro.verron.docxstamper to pro.verron.officestamper.test;

    exports pro.verron.docxstamper.api;
    exports pro.verron.docxstamper.preset;

    /**
     * TODO: remove all the following exports in next version
     */
    opens pro.verron.docxstamper;
    exports pro.verron.docxstamper;
    exports org.wickedsource.docxstamper;
    exports org.wickedsource.docxstamper.api;
    exports org.wickedsource.docxstamper.api.commentprocessor;
    exports org.wickedsource.docxstamper.el;
    exports org.wickedsource.docxstamper.util;
    exports org.wickedsource.docxstamper.processor;
    exports org.wickedsource.docxstamper.processor.table;
}
