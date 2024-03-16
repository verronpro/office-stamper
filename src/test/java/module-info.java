module pro.verron.docxstamper.test {
    requires transitive pro.verron.docxstamper;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires spring.core;
    requires spring.context;
    requires spring.expression;

    requires org.docx4j.openxml_objects;
    requires org.docx4j.core;

    requires org.slf4j;
    requires jakarta.xml.bind;

    opens pro.verron.docxstamper.test;

    // exports pro.verron.docxstamper.test;
    // exports pro.verron.docxstamper.test.commentProcessors;
    // exports pro.verron.docxstamper.test.utils.context;
    // exports org.wickedsource.docxstamper.test;
}
