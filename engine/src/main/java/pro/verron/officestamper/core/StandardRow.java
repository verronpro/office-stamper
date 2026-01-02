package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.List;

import static java.util.stream.Collectors.toList;

/// Represents a standard row in a table. This class provides functionality for manipulating table rows in a document.
public class StandardRow
        implements Table.Row {
    private final DocxPart part;
    private final Tbl tbl;
    private final Tr tr;


    /// Constructs a new StandardRow with the specified table row element.
    ///
    /// @param tr the table row element to wrap
    public StandardRow(DocxPart part, Tbl tbl, Tr tr) {
        this.part = part;
        this.tbl = tbl;
        this.tr = tr;
    }

    @Override
    public void remove() {
        WmlUtils.remove(this.tr);
    }

    @Override
    public Table table() {
        return new StandardTable(tbl);
    }

    @Override
    public Table.Row copy() {
        return new StandardRow(part, tbl, XmlUtils.deepCopy(tr));
    }

    @Override
    public void removeComment(Comment comment) {
        WmlUtils.deleteCommentFromElements(comment.getId(), tr.getContent());
    }

    @Override
    public List<Hook> hooks() {
        return Hooks.ofHooks(tr)
                    .collect(toList());
    }

    @Override
    public Tr asTr() {
        return tr;
    }
}
