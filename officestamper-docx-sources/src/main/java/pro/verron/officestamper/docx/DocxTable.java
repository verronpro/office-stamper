package pro.verron.officestamper.docx;

import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.docx.wml.WmlUtils;

import java.util.List;

/// docx4j 11 implementation of {@link Table} (port of the engine's `StandardTable`).
final class DocxTable
        implements Table {

    private final DocxPart part;
    private final Tbl tbl;

    DocxTable(DocxPart part, Tbl tbl) {
        this.part = part;
        this.tbl = tbl;
    }

    @Override
    public void remove() {
        WmlUtils.remove(tbl);
    }

    @Override
    public List<Row> rows() {
        return tbl.getContent()
                .stream()
                .filter(Tr.class::isInstance)
                .map(Tr.class::cast)
                .<Row>map(tr -> new DocxRow(part, tbl, tr))
                .toList();
    }

    @Override
    public int indexOf(Row row) {
        return tbl.getContent().indexOf(DocxElement.rawOf(row));
    }

    @Override
    public void addAll(int index, List<Row> rows) {
        for (int i = 0; i < rows.size(); i++) {
            var raw = DocxElement.rawOf(rows.get(i));
            JaxbCompat.setParent(raw, tbl);
            tbl.getContent().add(index + i, raw);
        }
    }

    @Override
    public void add(int index, Row row) {
        var raw = DocxElement.rawOf(row);
        JaxbCompat.setParent(raw, tbl);
        tbl.getContent().add(index, raw);
    }
}
