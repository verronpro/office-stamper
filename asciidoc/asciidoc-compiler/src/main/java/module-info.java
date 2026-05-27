module pro.verron.officestamper.asciidoc.compiler {
    requires pro.verron.officestamper.asciidoc.core;
    requires pro.verron.officestamper.asciidoc.converters;
    requires pro.verron.officestamper.asciidoc.docx;
    requires pro.verron.officestamper.utils;
    requires org.apache.xmlgraphics.batik.transcoder;
    requires org.apache.xmlgraphics.batik.codec;
    requires java.desktop;
    requires org.docx4j.core;

    exports pro.verron.officestamper.asciidoc.compiler;
}
