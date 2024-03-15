module pro.verron.opcstamper {
    exports pro.verron.docxstamper.api;
    exports pro.verron.docxstamper.preset;

    exports org.wickedsource.docxstamper;
    exports org.wickedsource.docxstamper.el;
    exports org.wickedsource.docxstamper.processor.table;
    exports org.wickedsource.docxstamper.util;
    exports org.wickedsource.docxstamper.api.commentprocessor;
    // exports org.wickedsource.docxstamper.processor;
    exports org.wickedsource.docxstamper.api;

    requires spring.core;
    requires spring.expression;

    requires transitive org.docx4j.core;

    requires static org.apache.commons.io;
    requires static org.slf4j;
    requires static jakarta.xml.bind;
}
