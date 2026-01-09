module pro.verron.officestamper.test {
    requires pro.verron.officestamper;
    requires org.objectweb.asm;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires spring.core;
    requires spring.context;
    requires spring.expression;

    requires org.docx4j.openxml_objects;
    requires org.docx4j.core;

    requires org.slf4j;
    requires jakarta.xml.bind;
    requires com.tngtech.archunit.junit5.api;
    requires com.tngtech.archunit;
    requires pro.verron.officestamper.asciidoc;
    requires pro.verron.officestamper.utils;

    opens pro.verron.officestamper.test;
    exports pro.verron.officestamper.test;
    exports pro.verron.officestamper.test.utils;
    opens pro.verron.officestamper.test.utils;
    exports pro.verron.officestamper.test.architecture;
    opens pro.verron.officestamper.test.architecture;
}
