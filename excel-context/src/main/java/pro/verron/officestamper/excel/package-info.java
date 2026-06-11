/// Lazy, query-oriented access to XLSX workbook data for template stamping.
///
/// Provides [ExcelContext], which exposes sheets, named tables, and cells (by
/// A1 notation) as a data source, along with [SheetContext] for worksheet
/// -level queries and [ExcelMergeStrategy] for combining multiple sheets.
///
/// Ensures non-null values by default.
@NullMarked
package pro.verron.officestamper.excel;

import org.jspecify.annotations.NullMarked;
