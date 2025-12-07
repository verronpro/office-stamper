package pro.verron.officestamper.preset.processors.table;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.preset.StampTable;
import pro.verron.officestamper.utils.WmlFactory;

import java.util.List;

import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// TableResolver class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.2
public class TableResolver
        extends CommentProcessor
        implements CommentProcessorFactory.ITableResolver {

    public TableResolver(ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void resolveTable(@Nullable StampTable givenTable) {
        var tbl = paragraph().parent(Tbl.class)
                             .orElseThrow(throwing("Paragraph is not within a table!"));
        if (givenTable != null) {
            replaceTableInplace(tbl, givenTable);
        }
        else {
            List<Object> tableParentContent = ((ContentAccessor) tbl.getParent()).getContent();
            tableParentContent.remove(tbl);
        }
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
        tableCellContent.add(WmlFactory.newParagraph(content));
    }
}
