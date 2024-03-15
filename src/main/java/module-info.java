module pro.verron.opcstamper {
    requires spring.core;
    requires spring.expression;

    requires transitive org.docx4j.core;

    requires static org.apache.commons.io;
    requires static org.slf4j;
    requires static jakarta.xml.bind;

    opens pro.verron.docxstamper.api;
    opens pro.verron.docxstamper.preset;

    exports pro.verron.docxstamper.api;
    exports pro.verron.docxstamper.preset;

    exports org.wickedsource.docxstamper;
    // exports org.wickedsource.docxstamper.api;
    // exports org.wickedsource.docxstamper.api.commentprocessor;
    // exports org.wickedsource.docxstamper.el;
    // exports org.wickedsource.docxstamper.util;
    // exports org.wickedsource.docxstamper.processor;
    // exports org.wickedsource.docxstamper.processor.table;


}
