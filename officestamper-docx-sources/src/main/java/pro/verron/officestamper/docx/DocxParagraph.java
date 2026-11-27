package pro.verron.officestamper.docx;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.Content;
import pro.verron.officestamper.api.Element;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.docx.wml.DocxIterator;
import pro.verron.officestamper.docx.wml.WmlUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;

/// docx4j 11 implementation of [Paragraph] (port of the engine's `StandardParagraph`).
final class DocxParagraph
        implements Paragraph {

    private final DocxPart part;
    private final P p;

    DocxParagraph(DocxPart part, P p) {
        this.part = part;
        this.p = p;
    }

    /// Finds the paragraph enclosing the given object, walking up the content tree (through smart
    /// tags and SDTs).
    static DocxParagraph enclosing(DocxPart part, Object object) {
        return switch (object) {
            case P paragraph -> new DocxParagraph(part, paragraph);
            case JAXBElement<?> jaxb -> enclosing(part, jaxb.getValue());
            case Object o when JaxbCompat.parentOf(o) != null -> enclosing(part, JaxbCompat.parentOf(o));
            default -> throw new IllegalStateException(
                    "Could not find an enclosing paragraph for " + object);
        };
    }

    private static List<Object> raws(List<Element> elements) {
        return elements.stream().map(DocxElement::rawOf).toList();
    }

    /// The insert's explicit run properties win; otherwise the formatting of the replaced run is
    /// preserved (the docx4j-free equivalent of the legacy `Insert.setRPr` copy-through).
    private static void applyRunProperties(Insert insert, List<Object> raws, RPr rPr) {
        if (insert.runProperties() != null) return;
        for (Object raw : raws) {
            if (raw instanceof R run) run.setRPr(rPr);
        }
    }

    @Override
    public void replace(List<Element> toRemove, List<Element> toAdd) {
        var content = new DocxElement(p.getParent(), part).content();
        var index = content.indexOf(new DocxElement(p, part));
        if (index < 0) throw new IllegalStateException("The paragraph is not attached to its parent content");
        content.addAll(index, toAdd);
        for (var element : toRemove) content.remove(element);
    }

    @Override
    public void remove() {
        WmlUtils.remove(p);
    }

    @Override
    public void replace(String expression, Insert insert) {
        var raws = raws(insert.elements());
        WmlUtils.replaceExpressionWithRun(p, expression, raws, rPr -> applyRunProperties(insert, raws, rPr));
    }

    @Override
    public void replace(Element start, Element end, Insert insert) {
        var content = p.getContent();
        // Anchors may be nested inside a hook tag run wrapping the comment range start, so fall
        // back to the top-level element containing the anchor (depth search) before failing.
        var fromIndex = indexContaining(content, DocxElement.rawOf(start));
        var toIndex = indexContaining(content, DocxElement.rawOf(end));
        if (fromIndex < 0) throw new IllegalStateException(
                "The start element (" + start.text() + ") is not in the paragraph");
        if (toIndex < 0) throw new IllegalStateException(
                "The end element (" + end.text() + ") is not in the paragraph");
        if (fromIndex > toIndex) throw new IllegalStateException(
                "The start element (" + start.text() + ") is after the end element (" + end.text() + ")");
        var raws = raws(insert.elements());
        ContentAccessor scope = () -> content.subList(fromIndex, toIndex);
        WmlUtils.replaceExpressionWithRun(scope, extractExpression(fromIndex, toIndex), raws,
                rPr -> applyRunProperties(insert, raws, rPr));
    }

    /// Index of the given raw object in the paragraph's top-level content, or of the top-level
    /// element whose subtree contains it.
    private static int indexContaining(List<Object> content, Object raw) {
        var direct = content.indexOf(raw);
        if (direct >= 0) return direct;
        for (var i = 0; i < content.size(); i++) {
            if (DocxDocumentUtil.depthElementSearch(raw, content.get(i))) return i;
        }
        return -1;
    }

    private String extractExpression(int fromIndex, int toIndex) {
        ContentAccessor scope = () -> p.getContent().subList(fromIndex, toIndex + 1);
        return new DocxIterator(scope)
                .selectClass(R.class)
                .map(WmlUtils::asString)
                .collect(joining());
    }

    @Override
    public String asString() {
        return WmlUtils.asString(p);
    }

    @Override
    public Content content() {
        return new DocxElement(p, part).content();
    }

    @Override
    public void apply(Consumer<Element> consumer) {
        for (var element : new DocxElement(p, part).content().elements()) {
            consumer.accept(element);
        }
    }

    @Override
    public Collection<Comment> getComments() {
        return DocxComments.findFor(p, part.part().getPackage())
                .stream()
                .<Comment>map(DocxComment::new)
                .toList();
    }

    @Override
    public Optional<Table.Row> parentTableRow() {
        return WmlUtils.getFirstParentWithClass(p, Tr.class, Integer.MAX_VALUE)
                .map(tr -> new DocxRow(part, (Tbl) tr.getParent(), tr));
    }

    @Override
    public Optional<Table> parentTable() {
        return WmlUtils.getFirstParentWithClass(p, Tbl.class, Integer.MAX_VALUE)
                .map(tbl -> new DocxTable(part, tbl));
    }

    @Override
    public String toString() {
        return asString();
    }
}
