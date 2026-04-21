module pro.verron.officestamper.excel {
    requires pro.verron.officestamper; // engine API (exceptions)

    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires org.jspecify;

    exports pro.verron.officestamper.excel;
}
