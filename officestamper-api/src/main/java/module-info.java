/// The docx4j-independent public API (seam) for OfficeStamper.
///
/// This module must never depend on org.docx4j (or any other OOXML library).
/// Concrete providers live in separate modules (officestamper-docx4j11, ...).
module pro.verron.officestamper.api {
    exports pro.verron.officestamper.spi;
    uses pro.verron.officestamper.spi.OfficeStamper;
}
