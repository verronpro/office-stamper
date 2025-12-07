module pro.verron.officestamper.asciidoc {
    requires javafx.controls;
    requires javafx.graphics;
    requires org.asciidoctor.asciidoctorj.api;
    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;

    opens pro.verron.officestamper.asciidoc;
    exports pro.verron.officestamper.asciidoc;
}
