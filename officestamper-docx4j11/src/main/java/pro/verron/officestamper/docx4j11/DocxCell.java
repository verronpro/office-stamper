package pro.verron.officestamper.docx4j11;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Tc;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.Element;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.docx4j11.wml.WmlUtils;

import java.util.List;

/// docx4j 11 implementation of {@link Table.Cell}.
final class DocxCell
        extends DocxElement
        implements Table.Cell {

    DocxCell(DocxPart part, Tc tc) {
        super(tc, part);
    }

    @Override
    public Table.Cell copy() {
        return new DocxCell(owner(), (Tc) XmlUtils.deepCopy(raw()));
    }

    @Override
    public void setContent(List<Element> elements) {
        var tc = (Tc) raw();
        var content = tc.getContent();
        content.clear();
        for (var element : elements) {
            var raw = DocxElement.rawOf(element);
            if (raw instanceof Child child) child.setParent(tc);
            content.add(raw);
        }
        // An OOXML cell must contain at least a paragraph or a table.
        WmlUtils.ensureValidity(tc);
    }
}
