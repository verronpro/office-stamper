package pro.verron.officestamper.preset.processors.table;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.AbstractCommentProcessor;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.core.PlaceholderReplacer;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.preset.StampTable;
import pro.verron.officestamper.utils.WmlFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// TableResolver class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.2
public class TableResolver
        extends AbstractCommentProcessor
        implements CommentProcessorFactory.ITableResolver {
    private final Map<Tbl, StampTable> cols = new HashMap<>();
    private final Function<Tbl, List<Object>> nullSupplier;

    private TableResolver(
            ParagraphPlaceholderReplacer placeholderReplacer, Function<Tbl, List<Object>> nullSupplier
    ) {
        super(placeholderReplacer);
        this.nullSupplier = nullSupplier;
    }

    /// Generate a new [TableResolver] instance where value is replaced by an empty list when <code>null</code>
    ///
    /// @param pr a [PlaceholderReplacer] instance
    ///
    /// @return a new [TableResolver] instance
    public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
        return new TableResolver(pr, table -> Collections.emptyList());
    }

    @Override public void resolveTable(@Nullable StampTable givenTable) {
        var tbl = this.getParagraph()
                      .parent(Tbl.class)
                      .orElseThrow(throwing("Paragraph is not within a table!"));
        cols.put(tbl, givenTable);
    }

    @Override public void commitChanges(DocxPart document) {
        for (Map.Entry<Tbl, StampTable> entry : cols.entrySet()) {
            Tbl wordTable = entry.getKey();

            StampTable stampedTable = entry.getValue();

            if (stampedTable != null) {
                replaceTableInplace(wordTable, stampedTable);
            }
            else {
                List<Object> tableParentContent = ((ContentAccessor) wordTable.getParent()).getContent();
                int tablePosition = tableParentContent.indexOf(wordTable);
                List<Object> toInsert = nullSupplier.apply(wordTable);
                tableParentContent.set(tablePosition, toInsert);
            }
        }
    }

    @Override public void reset() {
        cols.clear();
    }

    private void replaceTableInplace(Tbl wordTable, StampTable stampedTable) {
        var headers = stampedTable.headers();

        var rows = wordTable.getContent();
        var headerRow = (Tr) rows.get(0);
        var firstDataRow = (Tr) rows.get(1);

        growAndFillRow(headerRow, headers);

        if (stampedTable.isEmpty()) rows.remove(firstDataRow);
        else {
            growAndFillRow(firstDataRow, stampedTable.getFirst());
            for (var rowContent : stampedTable.subList(1, stampedTable.size()))
                rows.add(copyRowFromTemplate(firstDataRow, rowContent));
        }
    }

    private void growAndFillRow(Tr row, List<String> values) {
        List<Object> cellRowContent = row.getContent();

        //Replace text in first cell
        JAXBElement<Tc> cell0 = (JAXBElement<Tc>) cellRowContent.getFirst();
        Tc cell0tc = cell0.getValue();
        setCellText(cell0tc, values.isEmpty() ? "" : values.getFirst());

        if (values.size() > 1) {
            //Copy the first cell and replace content for each remaining value
            for (String cellContent : values.subList(1, values.size())) {
                JAXBElement<Tc> xmlCell = XmlUtils.deepCopy(cell0);
                setCellText(xmlCell.getValue(), cellContent);
                cellRowContent.add(xmlCell);
            }
        }
    }

    private Tr copyRowFromTemplate(Tr firstDataRow, List<String> rowContent) {
        Tr newXmlRow = XmlUtils.deepCopy(firstDataRow);
        List<Object> xmlRow = newXmlRow.getContent();
        for (int i = 0; i < rowContent.size(); i++) {
            String cellContent = rowContent.get(i);
            Tc xmlCell = ((JAXBElement<Tc>) xmlRow.get(i)).getValue();
            setCellText(xmlCell, cellContent);
        }
        return newXmlRow;
    }

    private void setCellText(Tc tableCell, String content) {
        var tableCellContent = tableCell.getContent();
        tableCellContent.clear();
        tableCellContent.add(WmlFactory.newParagraph(new String[]{content}));
    }
}
