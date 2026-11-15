package pro.verron.officestamper.docx4j11;

import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.P;
import org.docx4j.wml.R.CommentReference;
import pro.verron.officestamper.spi.AnchoredComment;
import pro.verron.officestamper.spi.Element;
import pro.verron.officestamper.spi.Paragraph;
import pro.verron.officestamper.utils.wml.WmlFactory;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/// docx4j 11 implementation of {@link AnchoredComment} (port of the engine's `StandardComment`
/// and the comment-surgery parts of `CommentUtil`).
final class DocxAnchoredComment
        implements AnchoredComment {

    private final DocxPart part;
    private final Comments.Comment comment;
    private final CommentRangeStart start;
    private final CommentRangeEnd end;
    private final CommentReference reference;
    private final CTSmartTagRun startTagRun;

    private DocxAnchoredComment(
            DocxPart part,
            Comments.Comment comment,
            CommentRangeStart start,
            CommentRangeEnd end,
            CommentReference reference,
            CTSmartTagRun startTagRun
    ) {
        this.part = part;
        this.comment = comment;
        this.start = start;
        this.end = end;
        this.reference = reference;
        this.startTagRun = startTagRun;
    }

    /// Creates a comment skeleton for an inline hook tag (port of the legacy `StandardComment.create`):
    /// the markers are not attached to the document content; comment processors operate on the tag's
    /// own content through the anchored comment.
    static DocxAnchoredComment create(DocxPart part, ContentAccessor parent, String expression, BigInteger id) {
        var start = WmlFactory.newCommentRangeStart(id, parent);
        return new DocxAnchoredComment(part,
                WmlFactory.newComment(id, expression),
                start,
                WmlFactory.newCommentRangeEnd(id, parent),
                WmlFactory.newCommentReference(id, parent),
                WmlFactory.newSmartTag("officestamper", WmlFactory.newCtAttr("type", "processor"), start));
    }

    /// Wraps a comment found in the document (port of the legacy `CommentUtil.comment` resolution).
    static DocxAnchoredComment of(
            DocxPart part,
            CTSmartTagRun startTagRun,
            CommentRangeStart start,
            CommentRangeEnd end,
            Comments.Comment comment,
            CommentReference reference
    ) {
        return new DocxAnchoredComment(part, comment, start, end, reference, startTagRun);
    }

    @Override
    public String id() {
        return String.valueOf(comment.getId());
    }

    @Override
    public String author() {
        return comment.getAuthor();
    }

    @Override
    public String text() {
        return DocxComment.textOf(comment);
    }

    @Override
    public String expression() {
        return comment.getContent()
                .stream()
                .filter(P.class::isInstance)
                .map(P.class::cast)
                .map(WmlUtils::asString)
                .collect(joining());
    }

    @Override
    public Paragraph paragraph() {
        return DocxParagraph.enclosing(part, start.getParent());
    }

    @Override
    public Element startAnchor() {
        return new DocxElement(start, part);
    }

    @Override
    public Element endAnchor() {
        return new DocxElement(end, part);
    }

    @Override
    public List<Element> elements() {
        var parent = DocxDocumentUtil.findSmallestCommonParent(start, end);
        var elements = new ArrayList<Element>();
        boolean startFound = false;
        boolean endFound = false;
        for (var element : parent.getContent()) {
            startFound = startFound || DocxDocumentUtil.depthElementSearch(start, element);
            if (startFound && !endFound) elements.add(new DocxElement(element, part));
            endFound = endFound || DocxDocumentUtil.depthElementSearch(end, element);
        }
        return List.copyOf(elements);
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        if (type.isInstance(comment)) return type.cast(comment);
        if (type.isInstance(start)) return type.cast(start);
        if (type.isInstance(end)) return type.cast(end);
        if (type.isInstance(reference)) return type.cast(reference);
        if (type.isInstance(startTagRun)) return type.cast(startTagRun);
        throw new IllegalArgumentException("Cannot unwrap AnchoredComment as " + type.getName());
    }

    @Override
    public void delete() {
        var endParent = (ContentAccessor) end.getParent();
        endParent.getContent().remove(end);
        var startParent = (ContentAccessor) start.getParent();
        startParent.getContent().remove(start);
        // A hook tag that only wrapped this comment's range start is now empty: drop it too.
        if (startParent instanceof CTSmartTagRun tag && tag.getContent().isEmpty()) {
            ((ContentAccessor) tag.getParent()).getContent().remove(tag);
        }
        if (reference != null) {
            ((ContentAccessor) reference.getParent()).getContent().remove(reference);
        }
    }

    @Override
    public void deleteFrom(Element element) {
        var backing = DocxElement.backingList(DocxElement.rawOf(element));
        if (backing != null) WmlUtils.deleteCommentFromElements(comment.getId(), backing);
    }
}
