package pro.verron.officestamper.excel;

/// Strategies for merging multiple sheets in an Excel workbook.
public enum ExcelMergeStrategy {
    /// Each sheet is exposed as a separate key in the context map.
    MAP,
    /// Sheets are joined together into a single list of records.
    JOIN
}
