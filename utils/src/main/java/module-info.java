/// The `pro.verron.officestamper.utils` module.
///
/// Low-level utilities shared by the Office‑stamper engine and tools. It contains helpers for iterating and
/// manipulating WordprocessingML structures (docx4j), bit and function utilities, as well as general-purpose iterators
/// used by the higher-level modules.
///
///
/// Exports:
///
///     - `pro.verron.officestamper.utils.wml` – WML/Docx helpers and factories.
///     - `pro.verron.officestamper.utils.function` – functional helpers.
///     - `pro.verron.officestamper.utils.bit` – small bit/byte utilities.
///     - `pro.verron.officestamper.utils.iterator` – iterator utilities.
///
///
module pro.verron.officestamper.utils {
    requires jakarta.xml.bind;
    requires org.slf4j;
    requires org.docx4j.core;
    requires org.jspecify;

    opens pro.verron.officestamper.utils.iterator;
    exports pro.verron.officestamper.utils.iterator;

    opens pro.verron.officestamper.utils.wml;
    exports pro.verron.officestamper.utils.wml;

    opens pro.verron.officestamper.utils.function;
    exports pro.verron.officestamper.utils.function;

    opens pro.verron.officestamper.utils.bit;
    exports pro.verron.officestamper.utils.bit;
}
