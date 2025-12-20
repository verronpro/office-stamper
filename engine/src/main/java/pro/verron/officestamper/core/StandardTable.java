package pro.verron.officestamper.core;

import org.docx4j.wml.Tbl;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.utils.wml.WmlUtils;

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
}
