package pro.verron.officestamper.docx;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTSdtCell;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import pro.verron.officestamper.api.Content;
import pro.verron.officestamper.api.Element;
import pro.verron.officestamper.api.RunProperties;

import java.util.ArrayList;
import java.util.List;

/// docx4j 11 implementation of [Element].
public class DocxElement implements Element {

    private final Object value;
    private final DocxPart part;

    public DocxElement(Object value, DocxPart part) {
        this.value = value;
        this.part = part;
    }

    /// @return the raw docx4j object this element wraps
    Object raw() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DocxElement other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /// @return the part this element belongs to
    DocxPart owner() {
        return part;
    }

    /// @param element an spi element
    /// @return the raw docx4j object the given element wraps
    /// @throws IllegalArgumentException when the element comes from another provider
    public static Object rawOf(Element element) {
        if (element instanceof DocxElement docxElement) return docxElement.raw();
        throw new IllegalArgumentException(
                "Element is not a docx4j element: " + element.getClass().getName());
    }

    /// @param o a docx4j object
    /// @return the live backing list of the given object's content, or an empty immutable list when it
    ///         is not a container
    static List<Object> backingList(Object o) {
        if (!isContainer(o)) return List.of();
        if (o instanceof ContentAccessor ca) return ca.getContent();
        if (o instanceof CTSmartTagRun t) return t.getContent();
        if (o instanceof SdtRun s) return s.getSdtContent().getContent();
        if (o instanceof SdtBlock s) return s.getSdtContent().getContent();
        if (o instanceof CTSdtCell s) return s.getSdtContent().getContent();
        if (o instanceof P p) return p.getContent();
        if (o instanceof Tc tc) return tc.getContent();
        if (o instanceof Tr tr) return tr.getContent();
        if (o instanceof Tbl tbl) return tbl.getContent();
        if (o instanceof Body b) return b.getContent();
        throw new AssertionError("Not a container: " + o.getClass().getName());
    }

    /// @param o a docx4j object
    /// @return true when the given object exposes a mutable docx4j content list
    static boolean isContainer(Object o) {
        return o instanceof ContentAccessor || o instanceof CTSmartTagRun || o instanceof SdtRun
               || o instanceof SdtBlock || o instanceof CTSdtCell || o instanceof P
               || o instanceof Tc || o instanceof Tr || o instanceof Tbl || o instanceof Body;
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
    public Content content() {
        return new DocxContent(value, backingList(value), part);
    }

    @Override
    public Element parent() {
        var parent = JaxbCompat.parentOf(value);
        if (parent != null) {
            return new DocxElement(parent, part);
        }
        return null;
    }

    @Override
    public Element copy() {
        return new DocxElement(XmlUtils.deepCopy(value), part);
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
        for (Element child : children()) {
            var properties = child.runProperties();
            if (properties != null) return properties;
        }
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
        else if (o instanceof CTSmartTagRun t) out.addAll(t.getContent());
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
