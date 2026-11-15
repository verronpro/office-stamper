package pro.verron.officestamper.docx4j11;

import org.docx4j.wml.Body;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import pro.verron.officestamper.spi.Element;
import pro.verron.officestamper.spi.RunProperties;

import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of {@link Element}.
public final class DocxElement implements Element {

    private final Object value;
    private final DocxPart part;

    DocxElement(Object value, DocxPart part) {
        this.value = value;
        this.part = part;
    }

    @Override
    public List<Element> children() {
        var out = new ArrayList<Element>();
        for (Object o : directChildren(value)) {
            out.add(new DocxElement(o, part));
        }
        return List.copyOf(out);
    }

    @Override
    public String text() {
        if (value instanceof R r) return runText(r);
        if (value instanceof Text t) return t.getValue();
        if (value instanceof ContentAccessor ca) return concatText(ca);
        return "";
    }

    @Override
    public void setText(String text) {
        if (value instanceof R r) {
            Text t = r.getContent().stream()
                    .filter(Text.class::isInstance)
                    .map(Text.class::cast)
                    .findFirst()
                    .orElseGet(() -> {
                        var nt = new Text();
                        r.getContent().add(0, nt);
                        return nt;
                    });
            t.setValue(text);
        } else if (value instanceof Text t) {
            t.setValue(text);
        }
    }

    @Override
    public boolean isRun() {
        return value instanceof R;
    }

    @Override
    public RunProperties runProperties() {
        if (value instanceof R r && r.getRPr() != null) return new DocxRunProperties(r.getRPr());
        return null;
    }

    private static String runText(R r) {
        var sb = new StringBuilder();
        for (Object o : r.getContent()) {
            if (o instanceof Text t) sb.append(t.getValue());
        }
        return sb.toString();
    }

    private static String concatText(ContentAccessor ca) {
        var sb = new StringBuilder();
        for (Object o : ca.getContent()) {
            if (o instanceof R r) sb.append(runText(r));
            else if (o instanceof Text t) sb.append(t.getValue());
            else if (o instanceof ContentAccessor c) sb.append(concatText(c));
        }
        return sb.toString();
    }

    static List<Object> directChildren(Object o) {
        var out = new ArrayList<>();
        if (o instanceof ContentAccessor ca) out.addAll(ca.getContent());
        else if (o instanceof SdtRun s) out.addAll(s.getSdtContent().getContent());
        else if (o instanceof SdtBlock s) out.addAll(s.getSdtContent().getContent());
        else if (o instanceof CTSdtCell s) out.addAll(s.getSdtContent().getContent());
        else if (o instanceof P p) out.addAll(p.getContent());
        else if (o instanceof Tc tc) out.addAll(tc.getContent());
        else if (o instanceof Tr tr) out.addAll(tr.getContent());
        else if (o instanceof Tbl tbl) out.addAll(tbl.getContent());
        else if (o instanceof Body b) out.addAll(b.getContent());
        return out;
    }
}
