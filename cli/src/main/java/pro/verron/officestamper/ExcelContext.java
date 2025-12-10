package pro.verron.officestamper;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.jspecify.annotations.NonNull;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.Workbook;
import org.xlsx4j.sml.Worksheet;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.InputStream;
import java.util.*;

import static java.util.Collections.emptyList;

public class ExcelContext
        extends AbstractMap<String, List<Map<String, String>>> {

    public static final DataFormatter formatter = new DataFormatter();
    private final Map<String, List<Map<String, String>>> source;

    public ExcelContext(SpreadsheetMLPackage spreadsheetPackage) {
        var workbookPart = spreadsheetPackage.getWorkbookPart();
        var workbook = getWorkbook(workbookPart);
        var allSheets = workbook.getSheets();
        var sheets = allSheets.getSheet();

        var relationshipsPart = workbookPart.getRelationshipsPart();

        var excel = new TreeMap<String, List<Map<String, String>>>();

        for (var sheet : sheets) {
            var worksheetPart = extractWorksheetPart(sheet, relationshipsPart);
            var worksheet = extractWorksheet(worksheetPart);
            var sheetDate = worksheet.getSheetData();
            var rows = sheetDate.getRow();
            excel.put(sheet.getName(), extractRecords(rows));
        }

        source = Collections.unmodifiableMap(excel);
    }

    private static Workbook getWorkbook(WorkbookPart workbookPart) {
        try {
            return workbookPart.getContents();
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private WorksheetPart extractWorksheetPart(Sheet sheet, RelationshipsPart relationshipsPart) {
        return (WorksheetPart) relationshipsPart.getPart(sheet.getId());
    }

    private Worksheet extractWorksheet(WorksheetPart worksheetPart) {
        try {
            return worksheetPart.getContents();
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private List<Map<String, String>> extractRecords(List<Row> rows) {
        if (rows.isEmpty()) return emptyList();
        var headers = extractHeaders(rows.getFirst());
        return extractRecords(headers, rows.subList(1, rows.size()));
    }

    private List<String> extractHeaders(Row row) {
        return row.getC()
                  .stream()
                  .map(formatter::formatCellValue)
                  .toList();
    }

    private List<Map<String, String>> extractRecords(List<String> headers, List<Row> rows) {
        List<Map<String, String>> list = new ArrayList<>();
        for (var row : rows) {
            Map<String, String> rec = new TreeMap<>();
            for (var i = 0; i < headers.size(); i++) {
                rec.put(headers.get(i), formatCellValueAt(row, i));
            }
            list.add(rec);
        }
        return list;
    }

    private static String formatCellValueAt(Row row, int i) {
        var cells = row.getC();
        if (i >= cells.size()) return "";
        return formatter.formatCellValue(cells.get(i));
    }

    public static Object from(InputStream inputStream) {
        try {
            return from(SpreadsheetMLPackage.load(inputStream));
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private static Object from(SpreadsheetMLPackage spreadsheetPackage) {
        return new ExcelContext(spreadsheetPackage);
    }

    @Override
    public @NonNull Set<Entry<String, List<Map<String, String>>>> entrySet() {
        return source.entrySet();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
