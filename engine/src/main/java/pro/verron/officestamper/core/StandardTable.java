package pro.verron.officestamper.core;

import org.docx4j.wml.Tbl;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.List;

/// The [StandardTable] class represents a table in a document and implements the [Table] interface. It provides
/// functionality to manipulate and interact with tables in documents.
public class StandardTable
        implements Table {
    private final Tbl tbl;


    /// Constructs a new [StandardTable] object with the specified [Tbl] object.
    ///
    /// @param tbl the [Tbl] object representing the table
    public StandardTable(Tbl tbl) {
        this.tbl = tbl;
    }

    @Override
    public void remove() {
        WmlUtils.remove(tbl);
    }

    @Override
    public int indexOf(Row row) {
        return tbl.getContent()
                  .indexOf(row.asTr());
    }

    @Override
    public void addAll(int index, List<Row> rows) {
        var trs = rows.stream()
                      .map(Row::asTr)
                      .toList();
        tbl.getContent()
           .addAll(index, trs);
    }

    @Override
    public void add(int index, Row row) {
        var tr = row.asTr();
        tbl.getContent()
           .add(index, tr);
    }
}
