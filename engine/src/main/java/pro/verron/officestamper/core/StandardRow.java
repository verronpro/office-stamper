package pro.verron.officestamper.core;

import org.docx4j.wml.Tr;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.utils.wml.WmlUtils;

/// Represents a standard row in a table. This class provides functionality for manipulating table rows in a document.
public class StandardRow
        implements Table.Row {
    private final Tr tr;


    /// Constructs a new StandardRow with the specified table row element.
    ///
    /// @param tr the table row element to wrap
    public StandardRow(Tr tr) {
        this.tr = tr;
    }

    @Override
    public void remove() {
        WmlUtils.remove(this.tr);
    }
}
