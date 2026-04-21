package pro.verron.officestamper.excel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExcelContext Tests")
class ExcelContextTest {

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

        @SuppressWarnings("unchecked")
        var firstSheet = (Map<String, Object>) sheets.getFirst();
        assertEquals("Hello", firstSheet.get("A1"));
        assertEquals("${name}", firstSheet.get("B1"));

        @SuppressWarnings("unchecked")
        var rows = (List<Map<String, String>>) firstSheet.get("rows");
        // sample file has no data rows beyond the first row
        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }
}
