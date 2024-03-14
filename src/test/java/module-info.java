module pro.verron.opcstamper.test {
    requires pro.verron.opcstamper;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires spring.context;
    requires spring.expression;
    requires org.docx4j.openxml_objects;
    requires org.docx4j.core;
    requires org.slf4j;
    requires jakarta.xml.bind;
    requires spring.core;

    opens pro.verron.docxstamper.test to
            spring.expression, org.junit.platform.commons;

    opens org.wickedsource.docxstamper.test to org.junit.platform.commons;
    opens pro.verron.docxstamper.test.utils.context to spring.core;

    exports pro.verron.docxstamper.test.utils.context to spring.expression;
    exports pro.verron.docxstamper.test.commentProcessors to pro.verron.opcstamper;
    exports pro.verron.docxstamper.test to pro.verron.opcstamper;
    exports org.wickedsource.docxstamper.test to spring.expression;
}
