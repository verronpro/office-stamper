package pro.verron.officestamper.docx;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.AnchoredComment;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.RunProperties;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Tag;
import pro.verron.officestamper.docx.wml.WmlUtils;

import java.math.BigInteger;
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
    public Paragraph paragraph() {
        return DocxParagraph.enclosing(owner(), tag().getParent());
    }

    @Override
    public String expression() {
        return WmlUtils.asString(tag().getContent());
    }

    @Override
    public AnchoredComment asComment() {
        var rangeStart = tag().getContent()
                              .stream()
                              .filter(CommentRangeStart.class::isInstance)
                              .map(CommentRangeStart.class::cast)
                              .findFirst();
        if (rangeStart.isPresent()) {
            var resolved = DocxHooks.resolve(owner(), rangeStart.get());
            if (resolved != null) return resolved;
        }
        return DocxAnchoredComment.create(owner(), (ContentAccessor) tag().getParent(), expression(),
                BigInteger.ZERO);
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
            JaxbCompat.setParent(raw, tag());
            if (raw instanceof R run) applyRunProperties(effective.runProperties(), run);
            content.add(raw);
        }
    }

    /// The provider applies the insert's run properties to the inserted runs (the formatting of the
    /// replaced expression is preserved through [DocxElement#runProperties]).
    private static void applyRunProperties(RunProperties properties, R run) {
        if (properties instanceof DocxRunProperties docxProperties) run.setRPr(docxProperties.rPr());
    }

    @Override
    public void remove() {
        WmlUtils.remove(raw());
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
