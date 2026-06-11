/// Lazy, query-oriented context over XLSX workbooks for OfficeStamper.
///
/// This module provides
/// [ExcelContext][pro.verron.officestamper.excel.ExcelContext], which
/// exposes an XLSX workbook as a data source for template stamping. Sheets,
/// named tables, and individual cells (by A1 notation) can be queried
/// lazily, making it suitable for large workbooks where only a subset of
/// data is needed. It also supports merging multiple sheets with
/// configurable strategies.
///
/// ## Exported Packages
/// - [pro.verron.officestamper.excel] - Lazy, query-oriented access to XLSX
/// workbook data
module pro.verron.officestamper.excel {
    requires org.docx4j.core;
    requires org.docx4j.openxml_objects;
    requires org.jspecify;

    exports pro.verron.officestamper.excel;
}
