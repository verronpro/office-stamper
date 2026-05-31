package pro.verron.officestamper.excel;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.jspecify.annotations.NonNull;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import java.util.*;

import static java.util.Collections.emptyList;

/// A lazy view over a worksheet.
///
/// Supports:
/// - Accessing a cell value by its A1 address using `get("A1")`.
/// - Accessing the default single-table representation via `get("rows")` (first row = headers).
final class SheetContext
        extends AbstractMap<String, Object> {

    private final String name;
    private final WorksheetPart worksheetPart;
    private List<Map<String, String>> rowsCache;

    SheetContext(String name, WorksheetPart worksheetPart) {
        this.name = name;
        this.worksheetPart = worksheetPart;
    }

    static List<Map<String, String>> extractRangeAsRecords(Worksheet worksheet, String a1Range) {
        // a1Range like A1:C10
        var parts = a1Range.split(":");
        var start = parts[0];
        var end = parts.length > 1 ? parts[1] : parts[0];
        var startRC = parseA1(start);
        var endRC = parseA1(end);

        var rows = worksheet.getSheetData()
                            .getRow();
        if (rows.isEmpty()) return emptyList();

        // Headers on first row of the range
        var headerRowIndex = startRC.rowIndex;
        var headers = new ArrayList<String>();
        for (int c = startRC.colIndex; c <= endRC.colIndex; c++) {
            headers.add(findCellByA1(worksheet, cellRef(c, headerRowIndex)).map(ExcelContext.FORMATTER::formatCellValue)
                                                                           .orElse(""));
        }

        List<Map<String, String>> out = new ArrayList<>();
        for (long r = headerRowIndex + 1; r <= endRC.rowIndex; r++) {
            Map<String, String> rec = new LinkedHashMap<>();
            for (int c = startRC.colIndex; c <= endRC.colIndex; c++) {
                var v = findCellByA1(worksheet, cellRef(c, r)).map(ExcelContext.FORMATTER::formatCellValue)
                                                              .orElse("");
                rec.put(headers.get(c - startRC.colIndex), v);
            }
            out.add(rec);
        }
        return out;
    }

    private static RC parseA1(String a1) {
        int i = 0;
        int col = 0;
        while (i < a1.length() && Character.isLetter(a1.charAt(i))) {
            col = col * 26 + (Character.toUpperCase(a1.charAt(i)) - 'A' + 1);
            i++;
        }
        long row = Long.parseLong(a1.substring(i));
        return new RC(col - 1, row);
    }

    private static Optional<Cell> findCellByA1(Worksheet worksheet, String a1) {
        for (Row r : worksheet.getSheetData()
                              .getRow()) {
            for (Cell c : r.getC()) {
                if (a1.equalsIgnoreCase(c.getR())) return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    private static String cellRef(int colIndex, long rowIndex1Based) {
        return toColLetters(colIndex) + rowIndex1Based;
    }

    private static String toColLetters(int colIndex) {
        var sb = new StringBuilder();
        int n = colIndex + 1;
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    String name() {return name;}

    WorksheetPart worksheetPart() {return worksheetPart;}

    @Override
    public Object get(Object key) {
        if (!(key instanceof String k)) return null;
        if ("rows".equals(k)) return rows();
        // treat as A1 reference
        return findCellByA1(k).map(ExcelContext.FORMATTER::formatCellValue)
                              .orElse("");
    }

    @Override
    public @NonNull Set<Entry<String, Object>> entrySet() {
        // dynamic view: only advertise rows
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rows", rows());
        return m.entrySet();
    }

    private List<Map<String, String>> rows() {
        if (rowsCache != null) return rowsCache;
        var rows = sheetData().getRow();
        if (rows.isEmpty()) return emptyList();
        var headers = rows.getFirst()
                          .getC()
                          .stream()
                          .map(ExcelContext.FORMATTER::formatCellValue)
                          .toList();
        List<Map<String, String>> list = new ArrayList<>();
        for (int r = 1; r < rows.size(); r++) {
            var row = rows.get(r);
            Map<String, String> rec = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                List<Cell> rowCells = row.getC();
                Optional<Cell> cell;
                if (c >= rowCells.size()) cell = Optional.empty();
                else cell = Optional.of(rowCells.get(c));
                rec.put(headers.get(c),
                        cell.map(SheetContext::formatCellValueAt)
                            .orElse(""));
            }
            list.add(rec);
        }
        rowsCache = Collections.unmodifiableList(list);
        return rowsCache;
    }

    private SheetData sheetData() {return worksheet().getSheetData();}

    static String formatCellValueAt(Cell cell) {
        return ExcelContext.FORMATTER.formatCellValue(cell);
    }

    private Worksheet worksheet() {
        try {
            return worksheetPart.getContents();
        } catch (Docx4JException e) {
            throw new ExcelException(e);
        }
    }

    private Optional<Cell> findCellByA1(String a1) {
        for (Row r : sheetData().getRow()) {
            for (Cell c : r.getC()) {
                if (a1.equalsIgnoreCase(c.getR())) return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    private record RC(int colIndex, long rowIndex) {}
}
