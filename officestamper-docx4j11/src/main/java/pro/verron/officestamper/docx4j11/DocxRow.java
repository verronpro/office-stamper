package pro.verron.officestamper.docx4j11;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.docx4j11.wml.WmlUtils;

import java.util.List;

/// docx4j 11 implementation of {@link Table.Row} (port of the engine's `StandardRow`).
final class DocxRow
        extends DocxElement
        implements Table.Row {

    private final Tbl tbl;

    DocxRow(DocxPart part, Tbl tbl, Tr tr) {
        super(tr, part);
        this.tbl = tbl;
    }

    @Override
    public void remove() {
        WmlUtils.remove((Tr) raw());
    }

    @Override
    public Table table() {
        return new DocxTable(owner(), tbl);
    }

    @Override
    public Table.Row copy() {
        return new DocxRow(owner(), tbl, (Tr) XmlUtils.deepCopy(raw()));
    }

    @Override
    public List<Table.Cell> cells() {
        return ((Tr) raw()).getContent()
                .stream()
                .map(DocxRow::unwrapCell)
                .filter(Tc.class::isInstance)
                .map(Tc.class::cast)
                .<Table.Cell>map(tc -> new DocxCell(owner(), tc))
                .toList();
    }

    private static Object unwrapCell(Object o) {
        return o instanceof JAXBElement<?> jaxb ? jaxb.getValue() : o;
    }
}
