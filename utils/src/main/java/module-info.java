module pro.verron.officestamper.utils {
    requires org.docx4j.core;
    requires org.jspecify;
    requires jakarta.xml.bind;
    requires org.slf4j;

    exports pro.verron.officestamper.utils.iterator;

    opens pro.verron.officestamper.utils.iterator;
    opens pro.verron.officestamper.utils.wml;
    opens pro.verron.officestamper.utils.bit;
    opens pro.verron.officestamper.utils.function;
    exports pro.verron.officestamper.utils.wml;
    exports pro.verron.officestamper.utils.function;
    exports pro.verron.officestamper.utils.bit;
}
