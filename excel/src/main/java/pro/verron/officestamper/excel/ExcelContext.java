package pro.verron.officestamper.excel;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.jspecify.annotations.NonNull;
import org.xlsx4j.org.apache.poi.ss.usermodel.DataFormatter;
import org.xlsx4j.sml.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/// ExcelContext exposes a lazy, query-oriented view over an XLSX workbook.
/// - Sheets are accessible by index via the `sheets` list, each as a [SheetContext].
/// - Sheets are also accessible by name at the root level, resolving to their default table (first row = headers).
/// - Cells on a sheet can be queried using A1 notation via `sheet.get("A1")`.
/// - A sheet's default table is available via the special key `"rows"` on a [SheetContext].
/// - Named tables, when present, are exposed at the root level by their table name, each as a list of records
/// mapping header names to row values.
///
/// The underlying XLSX data is accessed lazily: no pre-loading of all sheets occurs in the constructor; data is read
/// only when a property is actually queried.
public final class ExcelContext
        extends AbstractMap<String, Object> {

    static final DataFormatter FORMATTER = new DataFormatter();

    private final SpreadsheetMLPackage spreadsheet;
    private final Map<String, Object> rootCache = new TreeMap<>();
    private List<SheetContext> sheetsCache;

    /// Creates a context backed by a SpreadsheetMLPackage.
    private ExcelContext(SpreadsheetMLPackage spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /// Create a new context from a path to an `.xlsx` file.
    ///
    /// @param path Path to the Excel workbook file
    /// @return a new [ExcelContext] instance
    /// @throws ExcelException if the file cannot be read
    public static ExcelContext from(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return from(is);
        } catch (IOException e) {
            throw new ExcelException(e);
        }
    }

    /// Create a new context from an input stream containing an `.xlsx` file.
    ///
    /// The input stream is consumed during construction.
    ///
    /// @param inputStream Input stream of an Excel workbook
    /// @return a new [ExcelContext] instance
    /// @throws ExcelException if the stream cannot be parsed
    public static ExcelContext from(InputStream inputStream) {
        try {
            var pkg = SpreadsheetMLPackage.load(inputStream);
            return new ExcelContext(pkg);
        } catch (Docx4JException e) {
            throw new ExcelException(e);
        }
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String name)) return null;
        if ("sheets".equals(name)) return enumerateSheets();

        // Sheet by name -> default table
        var maybeSheet = enumerateSheets().stream()
                                          .filter(s -> s.name()
                                                        .equals(name))
                                          .findFirst();
        if (maybeSheet.isPresent()) {
            return rootCache.computeIfAbsent(name,
                    _ -> defaultTable(maybeSheet.get()
                                                .worksheetPart()));
        }

        // Named table at root: resolve lazily on demand
        return rootCache.computeIfAbsent(name, this::resolveNamedTableByNameOrNull);
    }

    @Override
    public @NonNull Set<Entry<String, Object>> entrySet() {
        // Compose a dynamic view consisting of: sheets (list), sheet names -> default tables, and discovered tables
        var map = new LinkedHashMap<String, Object>();
        map.put("sheets", enumerateSheets());
        for (var sc : enumerateSheets()) {
            map.computeIfAbsent(sc.name(), _ -> defaultTable(sc.worksheetPart()));
        }
        // include anything populated in cache (e.g., named tables resolved so far)
        map.putAll(rootCache);
        return map.entrySet();
    }

    private List<SheetContext> enumerateSheets() {
        if (sheetsCache != null) return sheetsCache;
        var wb = workbook();
        var sheets = wb.getSheets()
                       .getSheet();
        List<SheetContext> list = new ArrayList<>(sheets.size());
        for (Sheet sheet : sheets) {
            var ws = resolveWorksheetPart(sheet);
            list.add(new SheetContext(sheet.getName(), ws));
        }
        sheetsCache = Collections.unmodifiableList(list);
        return sheetsCache;
    }

    private List<Map<String, String>> defaultTable(WorksheetPart part) {
        var ws = worksheetOf(part);
        var rows = ws.getSheetData()
                     .getRow();
        if (rows.isEmpty()) return emptyList();
        var headers = extractHeaders(rows.getFirst());
        return toRecords(headers, rows.subList(1, rows.size()));
    }

    private Workbook workbook() {
        try {
            return workbookPart().getContents();
        } catch (Docx4JException e) {
            throw new ExcelException(e);
        }
    }

    private WorksheetPart resolveWorksheetPart(Sheet sheet) {
        var rels = relationshipsPart();
        return (WorksheetPart) rels.getPart(sheet.getId());
    }

    private static Worksheet worksheetOf(WorksheetPart part) {
        try {
            return part.getContents();
        } catch (Docx4JException e) {
            throw new ExcelException(e);
        }
    }

    private static List<String> extractHeaders(Row headerRow) {
        return headerRow.getC()
                        .stream()
                        .map(FORMATTER::formatCellValue)
                        .toList();
    }

    private static List<Map<String, String>> toRecords(List<String> headers, List<Row> rows) {
        return rows.stream()
                   .map(row -> {
                       LinkedHashMap<String, String> map = new LinkedHashMap<>();
                       int bound = headers.size();
                       for (int i = 0; i < bound; i++) {
                           var cell = findCell(row, i);
                           map.put(headers.get(i),
                                   cell.map(SheetContext::formatCellValueAt)
                                       .orElse(""));
                       }
                       return map;
                   })
                   .collect(Collectors.toCollection(() -> new ArrayList<>(rows.size())));
    }

    private WorkbookPart workbookPart() {
        return spreadsheet.getWorkbookPart();
    }

    private RelationshipsPart relationshipsPart() {
        return workbookPart().getRelationshipsPart();
    }

    private static @NonNull Optional<Cell> findCell(Row row, int i) {
        List<Cell> rowCells = row.getC();
        Optional<Cell> cell;
        if (i >= rowCells.size()) cell = Optional.empty();
        else cell = Optional.of(rowCells.get(i));
        return cell;
    }

    private Object resolveNamedTableByNameOrNull(String tableName) {
        // Attempt to find a table with this name on any worksheet
        for (SheetContext sc : enumerateSheets()) {
            var part = sc.worksheetPart();
            var ws = worksheetOf(part);
            var tableParts = ws.getTableParts();
            if (tableParts == null) continue;
            var rels = part.getRelationshipsPart();
            var tps = tableParts.getTablePart();
            for (var tp : tps) {
                var p = rels.getPart(tp.getId());
                // Content type in xlsx4j is org.xlsx4j.sml.Table
                try {
                    var table = (org.docx4j.openpackaging.parts.SpreadsheetML.TablePart) p;
                    var ct = table.getContents(); // org.xlsx4j.sml.Table
                    if (Objects.equals(ct.getName(), tableName)) {
                        var ref = ct.getRef(); // e.g., A1:C10
                        return SheetContext.extractRangeAsRecords(worksheetOf(part), ref);
                    }
                } catch (ClassCastException | Docx4JException e) {
                    // Ignore and continue; not a standard table part
                }
            }
        }
        return null;
    }
}
