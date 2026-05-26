package pro.verron.officestamper.excel;

import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    @DisplayName("Should inner join two lists of maps")
    void testInnerJoin() {
        var left = List.of(
                Map.of("ID", "1", "Name", "Alice"),
                Map.of("ID", "2", "Name", "Bob")
        );
        var right = List.of(
                Map.of("ID", "1", "Age", "25"),
                Map.of("ID", "2", "Age", "30")
        );

        var joined = ExcelContext.innerJoin(left, right, "ID");

        assertEquals(2, joined.size());
        assertEquals("Alice", joined.get(0).get("Name"));
        assertEquals("25", joined.get(0).get("Age"));
        assertEquals("Bob", joined.get(1).get("Name"));
        assertEquals("30", joined.get(1).get("Age"));
    }

    @Test
    @DisplayName("Should drop records that don't match the join key")
    void testInnerJoin_DropUnmatched() {
        var left = List.of(Map.of("ID", "1", "Name", "Alice"));
        var right = List.of(Map.of("ID", "2", "Age", "30"));

        var joined = ExcelContext.innerJoin(left, right, "ID");

        assertTrue(joined.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple matches (one-to-many)")
    void testInnerJoin_OneToMany() {
        var left = List.of(Map.of("ID", "1", "Name", "Alice"));
        var right = List.of(
                Map.of("ID", "1", "Task", "T1"),
                Map.of("ID", "1", "Task", "T2")
        );

        var joined = ExcelContext.innerJoin(left, right, "ID");

        assertEquals(2, joined.size());
        assertEquals("T1", joined.get(0).get("Task"));
        assertEquals("T2", joined.get(1).get("Task"));
    }

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
