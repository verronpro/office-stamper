package pro.verron.officestamper.excel;

import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xlsx4j.sml.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExcelContext Named Tables Tests")
class ExcelNamedTableTest {

    @Test
    @DisplayName("Should expose named table at root as list of records")
    void testNamedTableExposedAtRoot()
            throws Exception {
        // Arrange: load the shared sample and attach a named table over its header row (A1:B1)
        var bytes = createSampleWithNamedTable();

        // Act: build context from in-memory stream
        var ctx = ExcelContext.from(new ByteArrayInputStream(bytes));

        // Assert root access by table name (expect empty list since the table covers only headers)
        @SuppressWarnings("unchecked")
        var sampleTable = (List<Map<String, String>>) ctx.get("SampleTable");
        assertNotNull(sampleTable, "Named table should be available at root");
        assertTrue(sampleTable.isEmpty(), "Table over header row only should yield an empty list");

        // Sanity checks: A1 value and sheets listing still work
        var sheets = (List<?>) ctx.get("sheets");
        assertNotNull(sheets);
        assertFalse(sheets.isEmpty());
        @SuppressWarnings("unchecked")
        var firstSheet = (Map<String, Object>) sheets.getFirst();
        assertEquals("Hello", firstSheet.get("A1"));
    }

    private static byte[] createSampleWithNamedTable()
            throws Exception {
        // Load existing sample workbook (known-good formatting of cell strings)
        var samplePath = java.nio.file.Path.of("..", "test", "sources", "excel-base.xlsx")
                                           .normalize();
        SpreadsheetMLPackage pkg = SpreadsheetMLPackage.load(java.nio.file.Files.newInputStream(samplePath));

        // Resolve first worksheet part
        var wbPart = pkg.getWorkbookPart();
        var wb = wbPart.getContents();
        var sheetEl = wb.getSheets()
                        .getSheet()
                        .getFirst();
        var wsPart = (WorksheetPart) wbPart.getRelationshipsPart()
                                           .getPart(sheetEl.getId());
        var ws = wsPart.getContents();

        // Create a named table over A1:B1 (header-only)
        var table = new CTTable();
        table.setId(1L);
        table.setName("SampleTable");
        table.setDisplayName("SampleTable");
        table.setRef("A1:B1");

        CTTableColumns tcs = new CTTableColumns();
        tcs.setCount(2L);
        // Column names don't matter for the extraction since the range is header-only; set placeholders
        tcs.getTableColumn()
           .add(newTableColumn(1L, "Col1"));
        tcs.getTableColumn()
           .add(newTableColumn(2L, "Col2"));
        table.setTableColumns(tcs);

        var tablePart = new org.docx4j.openpackaging.parts.SpreadsheetML.TablePart(new PartName("/xl/tables/table1"
                                                                                                + ".xml"));
        tablePart.setContents(table);
        var rel = wsPart.addTargetPart(tablePart);

        // Wire Worksheet -> Table via tableParts
        var tableParts = new CTTableParts();
        tableParts.setCount(1L);
        var tp = new CTTablePart();
        tp.setId(rel.getId());
        tableParts.getTablePart()
                  .add(tp);
        ws.setTableParts(tableParts);

        // Save to zipped XLSX in-memory
        var saver = new SaveToZipFile(pkg);
        var baos = new ByteArrayOutputStream();
        saver.save(baos);
        return baos.toByteArray();
    }

    private static CTTableColumn newTableColumn(long id, String name) {
        CTTableColumn tc = new CTTableColumn();
        tc.setId(id);
        tc.setName(name);
        return tc;
    }
}
