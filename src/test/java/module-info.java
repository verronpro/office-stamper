module pro.verron.officestamper.test {
    requires pro.verron.officestamper;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires spring.core;
    requires spring.context;
    requires spring.expression;

    requires org.docx4j.openxml_objects;
    requires org.docx4j.core;

    requires org.slf4j;
    requires jakarta.xml.bind;

    opens pro.verron.officestamper.test;
    exports pro.verron.officestamper.test;
}
