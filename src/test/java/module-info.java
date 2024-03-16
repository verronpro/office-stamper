module pro.verron.opcstamper.test {
    requires transitive pro.verron.opcstamper;

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
}
