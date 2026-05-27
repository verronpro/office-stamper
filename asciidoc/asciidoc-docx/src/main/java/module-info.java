module pro.verron.officestamper.asciidoc.docx {
    requires org.jspecify;
    requires org.slf4j;

    requires transitive org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires jakarta.xml.bind;

    requires pro.verron.officestamper.asciidoc.core;
    requires pro.verron.officestamper.asciidoc.converters;

    exports pro.verron.officestamper.asciidoc.docx;
    opens pro.verron.officestamper.asciidoc.docx;
}
