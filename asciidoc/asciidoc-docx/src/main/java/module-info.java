module pro.verron.officestamper.asciidoc.docx {
    requires pro.verron.officestamper.asciidoc.core;
    requires pro.verron.officestamper.asciidoc.converters;
    requires transitive org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires org.slf4j;
    requires jakarta.xml.bind;
    requires org.jspecify;
    exports pro.verron.officestamper.asciidoc.docx;
}
