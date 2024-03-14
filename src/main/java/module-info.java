module pro.verron.opcstamper {
    exports pro.verron.docxstamper.api;
    exports pro.verron.docxstamper.preset;

    exports org.wickedsource.docxstamper;
    exports org.wickedsource.docxstamper.el;
    exports org.wickedsource.docxstamper.processor.table;
    exports org.wickedsource.docxstamper.util;
    exports org.wickedsource.docxstamper.api.commentprocessor;
    exports org.wickedsource.docxstamper.processor;
    exports org.wickedsource.docxstamper.api;


    requires org.apache.commons.io;
    requires org.docx4j.core;
    requires spring.expression;
    requires org.slf4j;
    requires spring.core;
    requires jakarta.xml.bind;
}
