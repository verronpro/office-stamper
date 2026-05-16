package pro.verron.officestamper.excel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Test class for the ExcelContext functionality. This class contains unit tests
/// to verify the behavior of the ExcelContext, ensuring proper handling of sheets,
/// cells, and rows extracted from an Excel file.
///
/// The tests include:
/// - Verification of sheet access by index.
/// - Validation of A1 cell values from the first sheet.
/// - Confirmation of default row handling based on headers.
///
/// These tests use a predefined sample Excel file (excel-base.xlsx) for verifying
/// the correctness and consistency of the ExcelContext's behavior.
@DisplayName("ExcelContext Tests") public class ExcelContextTest {

    @Test
    @DisplayName("Should expose sheets by index and A1 cells, and default rows by headers (existing sample)")
    void testSheetsAndCellsAndRows_onSample() {
        // Reuse the shared test asset from the repository
        Path sample = Path.of("..", "test", "sources", "excel-base.xlsx")
                          .normalize();
        var ctx = ExcelContext.from(sample);

        // Sheets by index
        var sheets = (List<?>) ctx.get("sheets");
        assertNotNull(sheets);
        assertFalse(sheets.isEmpty());

        @SuppressWarnings("unchecked") var firstSheet = (Map<String, Object>) sheets.getFirst();
        assertEquals("Hello", firstSheet.get("A1"));
        assertEquals("${name}", firstSheet.get("B1"));

        @SuppressWarnings("unchecked") var rows = (List<Map<String, String>>) firstSheet.get("rows");
        // sample file has no data rows beyond the first row
        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }
}
