package pro.verron.officestamper.preset.processors.table;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ElementFactory;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.preset.StampTable;

import java.util.List;

import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// TableResolver class.
///
/// @author Joseph Verron
/// @since 1.6.2
public class TableResolver
        extends CommentProcessor
        implements CommentProcessorFactory.ITableResolver {

    /// Constructs a new TableResolver with the given processor context.
    ///
    /// @param processorContext the context in which this processor operates
    public TableResolver(ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void resolveTable(@Nullable StampTable givenTable) {
        var table = paragraph().parentTable()
                               .orElseThrow(throwing("Paragraph is not within a table!"));
        if (givenTable != null) {
            replaceTableInplace(table, givenTable);
        }
        else {
            table.remove();
        }
    }

    private void replaceTableInplace(Table wordTable, StampTable stampedTable) {
        var headers = stampedTable.headers();
        var factory = context().part().factory();

        var rows = wordTable.rows();
        var headerRow = rows.getFirst();
        var firstDataRow = rows.get(1);

        growAndFillRow(headerRow, headers, factory);

        if (stampedTable.isEmpty()) firstDataRow.remove();
        else {
            growAndFillRow(firstDataRow, stampedTable.getFirst(), factory);
            int appended = rows.size();
            for (var rowContent : stampedTable.subList(1, stampedTable.size())) {
                wordTable.add(appended++, copyRowFromTemplate(firstDataRow, rowContent, factory));
            }
        }
    }

    private void growAndFillRow(Table.Row row, List<String> values, ElementFactory factory) {
        var cells = row.cells();

        // Replace text in first cell
        cells.getFirst()
             .setContent(List.of(factory.paragraph(values.isEmpty() ? "" : values.getFirst())));

        if (values.size() > 1) {
            // Copy the first cell and replace content for each remaining value
            var content = row.content();
            var firstCell = cells.getFirst();
            for (String cellContent : values.subList(1, values.size())) {
                var copy = firstCell.copy();
                copy.setContent(List.of(factory.paragraph(cellContent)));
                content.addAll(content.elements()
                                      .size(), List.of(copy));
            }
        }
    }

    private Table.Row copyRowFromTemplate(Table.Row firstDataRow, List<String> rowContent, ElementFactory factory) {
        Table.Row newXmlRow = firstDataRow.copy();
        var cells = newXmlRow.cells();
        for (int i = 0; i < rowContent.size(); i++) {
            String cellContent = rowContent.get(i);
            cells.get(i)
                 .setContent(List.of(factory.paragraph(cellContent)));
        }
        return newXmlRow;
    }
}
