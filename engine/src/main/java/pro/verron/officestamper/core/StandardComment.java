package pro.verron.officestamper.core;

import org.docx4j.wml.*;
import org.docx4j.wml.R.CommentReference;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.utils.wml.Content;
import pro.verron.officestamper.utils.wml.DocxDocument;
import pro.verron.officestamper.utils.wml.DocxDocument.Part;
import pro.verron.officestamper.utils.wml.Parent;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/// Standard implementation of the [Comment] interface. Represents a comment in a DOCX document with its associated
/// range markers and content.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @since 1.0.2
public class StandardComment implements Comment {

    private final DocxDocument.Comment comment;

    public StandardComment(DocxDocument.Comment comment) {
        this.comment = comment;
    }

    /// Creates a new instance of [StandardComment] and initializes it with the given parameters, including a comment,
    /// comment range start, comment range end, and a comment reference.
    ///
    /// @param part       the [Part] representing  the document section to which this comment belongs
    /// @param parent     the [Parent] representing the parent content of the comment range
    /// @param expression the [String] content to be included in the comment
    /// @param id         the unique [BigInteger] identifier for the comment
    /// @return a [StandardComment] instance initialized with the specified parameters
    public static StandardComment create(Part part, Content content, String expression, BigInteger id) {
        return new StandardComment(DocxDocument.Comment.createComment(part, content, expression, id));
    }

    @Override
    public Paragraph getParagraph() {
        var parent = comment.getCommentRangeStart().getParent();
        return StandardParagraph.from(comment.getPart(), parent);
    }

    @Override
    public CTSmartTagRun getStartTagRun() {
        return comment.getTagRun();
    }

    @Override
    public CommentRangeStart getCommentRangeStart() {
        return comment.getCommentRangeStart();
    }

    @Override
    public Content getContent() {
        return DocumentUtil.findSmallestCommonParent(comment.getCommentRangeStart(), comment.getCommentRangeEnd());
    }

    @Override
    public List<Content.Element> getElements() {
        List<Content.Element> elements = new ArrayList<>();
        boolean startFound = false;
        boolean endFound = false;
        var siblings = getContent().getContent();
        for (Content.Element element : siblings) {
            startFound = startFound || DocumentUtil.depthElementSearch(comment.getCommentRangeStart(), element);
            if (startFound && !endFound) elements.add(element);
            endFound = endFound || DocumentUtil.depthElementSearch(comment.getCommentRangeEnd(), element);
        }
        return elements;
    }

    @Override
    public CommentRangeEnd getCommentRangeEnd() {
        return comment.getCommentRangeEnd();
    }

    @Override
    public @Nullable CommentReference getCommentReference() {
        return comment.getCommentReference();
    }

    @Override
    public Comments.Comment getComment() {
        return comment.getWmlComment();
    }

    @Override
    public String expression() {
        return this.getComment()
                .getContent()
                .stream()
                .filter(P.class::isInstance)
                .map(P.class::cast)
                .map(p -> StandardParagraph.from(comment.getPart(), p))
                .map(StandardParagraph::asString)
                .collect(joining());
    }

    @Override
    public BigInteger getId() {
        return comment.getId();
    }
}
