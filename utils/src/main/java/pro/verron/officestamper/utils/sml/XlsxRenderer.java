package pro.verron.officestamper.utils.sml;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;

import static java.util.stream.Collectors.joining;

/// Utility class for rendering SpreadsheetML (Excel) files to string representations. This class provides methods to
/// convert Excel spreadsheets into human-readable formats.
public class XlsxRenderer {

    private XlsxRenderer() {
        throw new IllegalStateException("Utility class");
    }

    /// Converts the content of a SpreadsheetMLPackage into a string by extracting and formatting cell data from the
    /// Excel file.
    ///
    /// @param spreadsheet the Excel file represented as a [SpreadsheetMLPackage]
    ///
    /// @return a string representation of the cell content within the Excel spreadsheet
    public static String xlsxToString(SpreadsheetMLPackage spreadsheet) {
        var formatter = new DataFormatter();
        return new XlsxIterator(spreadsheet).filter(Cell.class::isInstance)
                                            .map(Cell.class::cast)
                                            .map(cell -> cell.getR() + ": " + formatter.formatCellValue(cell))
                                            .collect(joining("\n"));
    }
}
