package pro.verron.officestamper.core;

import org.docx4j.TextUtils;
import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Paragraph;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.wml.WmlFactory.*;

/// Standard implementation of the [Comment] interface. Represents a comment in a DOCX document with its associated
/// range markers and content.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.2
public class StandardComment
        implements Comment {
    private final DocxPart part;
    private final Comments.Comment comment;
    private final CommentRangeStart commentRangeStart;
    private final CommentRangeEnd commentRangeEnd;
    private final @Nullable CommentReference commentReference;
    private final CTSmartTagRun startTagRun;

    /// Constructs a new [StandardComment] object.
    ///
    /// @param part the [DocxPart] representing the document section this comment belongs to
    public StandardComment(
            DocxPart part,
            CTSmartTagRun startTagRun,
            CommentRangeStart commentRangeStart,
            CommentRangeEnd commentRangeEnd,
            Comments.Comment comment,
            @Nullable CommentReference commentReference
    ) {
        this.part = part;
        this.startTagRun = startTagRun;
        this.commentRangeStart = commentRangeStart;
        this.commentRangeEnd = commentRangeEnd;
        this.comment = comment;
        this.commentReference = commentReference;
    }

    /// Creates a new instance of [StandardComment] and initializes it with the given parameters, including a comment,
    /// comment range start, comment range end, and a comment reference.
    ///
    /// @param document the [DocxPart] representing the document to which this comment belongs
    /// @param parent the [ContentAccessor] representing the parent content of the comment range
    /// @param expression the [String] content to be included in the comment
    /// @param id the unique [BigInteger] identifier for the comment
    ///
    /// @return a [StandardComment] instance initialized with the specified parameters
    public static StandardComment create(DocxPart document, ContentAccessor parent, String expression, BigInteger id) {
        var start = newCommentRangeStart(id, parent);
        return new StandardComment(document,
                newSmartTag("officestamper", newCtAttr("type", "processor"), start),
                start,
                newCommentRangeEnd(id, parent),
                newComment(id, expression),
                newCommentReference(id, parent));
    }

    /// Generates a string representation of the [StandardComment] object, including its ID, content, and the amount
    /// children comment.
    ///
    /// @return a formatted string describing the [StandardComment]'s properties, including its ID, content, and the
    ///         size of its children.
    @Override
    public String toString() {
        return "StandardComment{comment={id=%s, content=%s}}}".formatted(comment.getId(),
                comment.getContent()
                       .stream()
                       .map(TextUtils::getText)
                       .collect(joining(",")));
    }

    @Override
    public Paragraph getParagraph() {
        var parent = commentRangeStart.getParent();
        return StandardParagraph.from(part, parent);
    }

    @Override
    public CTSmartTagRun getStartTagRun() {
        return startTagRun;
    }

    @Override
    public CommentRangeStart getCommentRangeStart() {
        return commentRangeStart;
    }

    @Override
    public ContentAccessor getParent() {
        return DocumentUtil.findSmallestCommonParent(commentRangeStart, commentRangeEnd);
    }

    @Override
    public List<Object> getElements() {
        List<Object> elements = new ArrayList<>();
        boolean startFound = false;
        boolean endFound = false;
        var siblings = getParent().getContent();
        for (Object element : siblings) {
            startFound = startFound || DocumentUtil.depthElementSearch(commentRangeStart, element);
            if (startFound && !endFound) elements.add(element);
            endFound = endFound || DocumentUtil.depthElementSearch(commentRangeEnd, element);
        }
        return elements;
    }

    @Override
    public CommentRangeEnd getCommentRangeEnd() {
        return commentRangeEnd;
    }

    @Override
    public @Nullable CommentReference getCommentReference() {
        return commentReference;
    }

    @Override
    public Comments.Comment getComment() {
        return comment;
    }

    @Override
    public String expression() {
        return this.getComment()
                   .getContent()
                   .stream()
                   .filter(P.class::isInstance)
                   .map(P.class::cast)
                   .map(p -> StandardParagraph.from(new TextualDocxPart(part.document()), p))
                   .map(StandardParagraph::asString)
                   .collect(joining());
    }
}
