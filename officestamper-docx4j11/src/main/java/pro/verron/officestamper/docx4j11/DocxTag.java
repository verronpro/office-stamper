package pro.verron.officestamper.docx4j11;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.spi.Insert;
import pro.verron.officestamper.spi.Tag;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.List;
import java.util.Objects;

/// docx4j 11 implementation of {@link Tag}: an `officestamper` smart tag run.
final class DocxTag
        extends DocxElement
        implements Tag {

    DocxTag(Object value, DocxPart part) {
        super(value, part);
    }

    private CTSmartTagRun tag() {
        return (CTSmartTagRun) raw();
    }

    @Override
    public String type() {
        return attribute("type");
    }

    @Override
    public String attribute(String name) {
        var smartTagPr = tag().getSmartTagPr();
        if (smartTagPr == null) return null;
        for (var attr : smartTagPr.getAttr()) {
            if (Objects.equals(attr.getName(), name)) return attr.getVal();
        }
        return null;
    }

    @Override
    public void setAttribute(String name, String value) {
        WmlUtils.setTagAttribute(tag(), name, value);
    }

    @Override
    public void replace(Insert insert) {
        var effective = insert.runProperties() != null
                ? insert
                : insert.withRunProperties(runProperties());
        var content = tag().getContent();
        content.clear();
        for (var element : effective.elements()) {
            var raw = DocxElement.rawOf(element);
            if (raw instanceof Child child) child.setParent(tag());
            content.add(raw);
        }
    }

    @Override
    public void remove() {
        WmlUtils.remove((Child) raw());
    }

    @Override
    public void inline() {
        var parent = tag().getParent();
        if (!(parent instanceof ContentAccessor accessor)) throw new IllegalStateException(
                "Cannot inline a tag whose parent is not a content holder: " + parent);
        var siblings = accessor.getContent();
        var index = siblings.indexOf(tag());
        // The tag may have been inlined by a previous pass when the same part was visited more than
        // once (e.g. a header shared by several sections).
        if (index < 0) return;
        siblings.remove(tag());
        siblings.addAll(index, tag().getContent());
    }
}
