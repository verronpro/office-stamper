package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.utils.WmlUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.stream;

/// Represents a textual part of a DOCX document, encapsulating the content and structure of the part while enabling
/// various operations such as accessing paragraphs, runs, and related parts. This class functions as a concrete
/// implementation of the [DocxPart] interface. It manages the association with the XML structure of a DOCX document.
public final class TextualDocxPart
        implements DocxPart {
    private final WordprocessingMLPackage document;
    private final Part part;
    private final ContentAccessor contentAccessor;

    /// Constructs a [TextualDocxPart] using the provided `document`. This constructor initializes the instance with the
    /// main document part and content accessor derived from the provided `WordprocessingMLPackage`.
    ///
    /// @param document the [WordprocessingMLPackage] representing the document to be processed.
    public TextualDocxPart(WordprocessingMLPackage document) {
        this(document, document.getMainDocumentPart(), document.getMainDocumentPart());
    }

    /// Constructs a [TextualDocxPart] using the specified `document`, `part`, and `contentAccessor`.
    ///
    /// @param document the [WordprocessingMLPackage] representing the document to be processed.
    /// @param part the specific part of the document being processed.
    /// @param contentAccessor the content accessor associated with the document part.
    public TextualDocxPart(WordprocessingMLPackage document, Part part, ContentAccessor contentAccessor) {
        this.document = document;
        this.part = part;
        this.contentAccessor = contentAccessor;
    }

    /// Returns the [WordprocessingMLPackage] instance representing the document associated with this part.
    ///
    /// @return the [WordprocessingMLPackage] instance representing the document.
    public WordprocessingMLPackage document() {return document;}

    /// Streams the parts of the document that match the specified relationship type, converting them into instances of
    /// [TextualDocxPart].
    ///
    /// @param types the type of relationship to filter and stream parts for.
    ///
    /// @return a stream of [DocxPart] instances representing the filtered and processed parts of the document.
    @Override
    public List<DocxPart> parts(String... types) {
        var mainDocument = document.getMainDocumentPart();
        var mainRelationships = mainDocument.getRelationshipsPart();
        return stream(types).map(mainRelationships::getRelationshipsByType)
                            .flatMap(Collection::stream)
                            .map(this::getPart)
                            .map(p -> new TextualDocxPart(document, p, (ContentAccessor) p))
                            .map(DocxPart.class::cast)
                            .toList();
    }

    @Override
    public void process(Consumer<DocxPart> processor) {
        var mainDocumentPart = document.getMainDocumentPart();
        processor.accept(new TextualDocxPart(document, mainDocumentPart, mainDocumentPart));
        mainDocumentPart.getRelationshipsPart()
                        .getRelationships()
                        .getRelationship()
                        .stream()
                        .map(this::getPart)
                        .filter(ContentAccessor.class::isInstance)
                        .map(p -> new TextualDocxPart(document, p, (ContentAccessor) p))
                        .forEach(processor);
    }


    @Override
    public Optional<Comment> comment(BigInteger id) {
        return Optional.ofNullable(comments().get(id));
    }

    static void onRangeStart(
            DocxPart source,
            CommentRangeStart crs,
            Map<BigInteger, StandardComment> allComments,
            Queue<StandardComment> stack,
            Map<BigInteger, Comment> rootComments
    ) {
        StandardComment comment = allComments.get(crs.getId());
        if (comment == null) {
            comment = new StandardComment(source);
            allComments.put(crs.getId(), comment);
            if (stack.isEmpty()) {
                rootComments.put(crs.getId(), comment);
            }
            else {
                stack.peek()
                     .addChild(comment);
            }
        }
        comment.setCommentRangeStart(crs);
        stack.add(comment);
    }

    static void onRangeEnd(
            CommentRangeEnd cre,
            Map<BigInteger, StandardComment> allComments,
            Queue<StandardComment> stack
    ) {
        StandardComment comment = allComments.get(cre.getId());
        if (comment == null)
            throw new OfficeStamperException("Found a comment range end before the comment range start !");

        comment.setCommentRangeEnd(cre);

        if (!stack.isEmpty()) {
            var peek = stack.peek();
            if (peek.equals(comment)) stack.remove();
            else throw new OfficeStamperException("Cannot figure which comment contains the other !");
        }
    }

    static void onReference(DocxPart source, R.CommentReference cr, Map<BigInteger, StandardComment> allComments) {
        StandardComment comment = allComments.get(cr.getId());
        if (comment == null) {
            comment = new StandardComment(source);
            allComments.put(cr.getId(), comment);
        }
        comment.setCommentReference(cr);
    }

    /// Retrieves the part associated with the specified relationship from the relationships part.
    ///
    /// @param r the relationship for which the associated part is to be retrieved.
    ///
    /// @return the part corresponding to the given relationship.
    public Part getPart(Relationship r) {
        return getRelationshipsPart().getPart(r);
    }

    private RelationshipsPart getRelationshipsPart() {
        return part().getRelationshipsPart();
    }

    /// Retrieves the part associated with this instance of the document part.
    ///
    /// @return the [Part] object representing the specific part associated with this instance.
    @Override
    public Part part() {return part;}

    /// Creates a new instance of [DocxPart] using the provided [ContentAccessor].
    ///
    /// @param accessor the content accessor associated with the document part to derive a new instance.
    ///
    /// @return a new instance of [DocxPart], specifically a [TextualDocxPart], initialized with the given content
    ///         accessor.
    @Override
    public DocxPart from(ContentAccessor accessor) {
        return new TextualDocxPart(document, part, accessor);
    }

    /// Retrieves the list of content objects associated with this document part.
    ///
    /// @return a list of objects representing the content of the document part.
    @Override
    public List<Object> content() {return contentAccessor.getContent();}

    @Override
    public String type() {
        return part.getRelationshipType();
    }

    @Override
    public Map<BigInteger, Comment> comments() {
        var rootComments = new HashMap<BigInteger, Comment>();
        var allComments = new HashMap<BigInteger, StandardComment>();
        var stack = Collections.asLifoQueue(new ArrayDeque<StandardComment>());

        var list = WmlUtils.extractCommentElements(document);
        for (Child commentElement : list) {
            if (commentElement instanceof CommentRangeStart crs)
                onRangeStart(this, crs, allComments, stack, rootComments);
            else if (commentElement instanceof CommentRangeEnd cre) onRangeEnd(cre, allComments, stack);
            else if (commentElement instanceof R.CommentReference cr) onReference(this, cr, allComments);
        }
        CommentUtil.getCommentsPart(document.getParts())
                   .map(CommentUtil::extractContent)
                   .map(Comments::getComment)
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(comment -> allComments.containsKey(comment.getId()))
                   .forEach(comment -> allComments.get(comment.getId())
                                                  .setComment(comment));
        return new HashMap<>(rootComments);
    }

    /// Computes the hash code for this object based on the `document`, `part`, and `contentAccessor` fields.
    ///
    /// @return an integer value representing the hash code of this object.
    @Override
    public int hashCode() {
        return Objects.hash(document, part, contentAccessor);
    }

    /// Compares this object with the specified object for equality. The comparison is based on the `document`, `part`,
    /// and `contentAccessor` fields of both objects.
    ///
    /// @param obj the object to be compared for equality with this instance.
    ///
    /// @return true if the specified object is equal to this object; false otherwise.
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextualDocxPart) obj;
        return Objects.equals(this.document, that.document) && Objects.equals(this.part, that.part) && Objects.equals(
                this.contentAccessor,
                that.contentAccessor);
    }

    /// Converts this instance of the [TextualDocxPart] class to its string representation. The string representation
    /// includes the name of the document, and the name of the part associated with this instance.
    ///
    /// @return a string representation of this instance, including the document name and part name formatted as
    ///         "DocxPart{doc=%s, part=%s}".
    @Override
    public String toString() {
        return "DocxPart{doc=%s, part=%s}".formatted(document.name(), part.getPartName());
    }
}
