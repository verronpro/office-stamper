package pro.verron.officestamper.core;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Paragraph;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a textual part of a DOCX document, encapsulating the content and structure of
 * the part while enabling various operations such as accessing paragraphs, runs, and related parts.
 * This class functions as a concrete implementation of the {@code DocxPart} interface.
 * It manages the association with the XML structure of a DOCX document.
 */
public final class TextualDocxPart
        implements DocxPart {
    private final WordprocessingMLPackage document;
    private final Part part;
    private final ContentAccessor contentAccessor;

    /**
     * Constructs a {@code TextualDocxPart} using the provided {@code document}.
     * This constructor initializes the instance with the main document part and content accessor
     * derived from the provided {@code WordprocessingMLPackage}.
     *
     * @param document the WordprocessingMLPackage representing the document to be processed.
     */
    public TextualDocxPart(WordprocessingMLPackage document) {
        this(document, document.getMainDocumentPart(), document.getMainDocumentPart());
    }

    /**
     * Constructs a {@code TextualDocxPart} using the specified {@code document}, {@code part},
     * and {@code contentAccessor}.
     *
     * @param document       the WordprocessingMLPackage representing the document to be processed.
     * @param part           the specific part of the document being processed.
     * @param contentAccessor the content accessor associated with the document part.
     */
    public TextualDocxPart(WordprocessingMLPackage document, Part part, ContentAccessor contentAccessor) {
        this.document = document;
        this.part = part;
        this.contentAccessor = contentAccessor;
    }

    /**
     * Returns the WordprocessingMLPackage instance representing the document
     * associated with this part.
     *
     * @return the WordprocessingMLPackage instance representing the document.
     */
    public WordprocessingMLPackage document() {return document;}

    /**
     * Streams the parts of the document that match the specified relationship type, converting them
     * into instances of {@link TextualDocxPart}.
     *
     * @param type the type of relationship to filter and stream parts for.
     * @return a stream of {@link DocxPart} instances representing the filtered and processed parts
     *         of the document.
     */
    public Stream<DocxPart> streamParts(String type) {
        return document.getMainDocumentPart()
                       .getRelationshipsPart()
                       .getRelationshipsByType(type)
                       .stream()
                       .map(this::getPart)
                       .map(p -> new TextualDocxPart(document, p, (ContentAccessor) p));
    }

    /**
     * Retrieves the part associated with the specified relationship from the relationships part.
     *
     * @param r the relationship for which the associated part is to be retrieved.
     * @return the part corresponding to the given relationship.
     */
    public Part getPart(Relationship r) {
        return getRelationshipsPart().getPart(r);
    }

    private RelationshipsPart getRelationshipsPart() {
        return part().getRelationshipsPart();
    }

    @Override
    public Part part() {return part;}

    @Override
    public DocxPart from(ContentAccessor accessor) {
        return new TextualDocxPart(document, part, accessor);
    }

    @Override
    public List<Object> content() {return contentAccessor.getContent();}

    /**
     * Streams all paragraphs contained in the document's main content or structured document tags (SDT).
     * The paragraphs are processed and transformed into instances of {@link Paragraph}.
     * This method combines paragraphs directly present in the document and paragraphs within SDT runs.
     *
     * @return a stream of {@link Paragraph} objects representing the paragraphs found within the document.
     */
    public Stream<Paragraph> streamParagraphs() {
        return Stream.concat(DocumentUtil.streamObjectElements(this, P.class)
                                         .map(p -> StandardParagraph.from(this, p)),
                DocumentUtil.streamObjectElements(this, SdtRun.class)
                            .map(SdtRun::getSdtContent)
                            .filter(CTSdtContentRun.class::isInstance)
                            .map(CTSdtContentRun.class::cast)
                            .map(paragraph -> StandardParagraph.from(this, paragraph)));
    }

    @Override
    public Stream<R> streamRun() {
        return DocumentUtil.streamObjectElements(this, P.class)
                           .map(P::getContent)
                           .flatMap(Collection::stream)
                           .filter(R.class::isInstance)
                           .map(R.class::cast);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, part, contentAccessor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextualDocxPart) obj;
        return Objects.equals(this.document, that.document) && Objects.equals(this.part, that.part) && Objects.equals(
                this.contentAccessor,
                that.contentAccessor);
    }

    @Override
    public String toString() {
        return "DocxPart{doc=%s, part=%s}".formatted(document.name(), part.getPartName());
    }

}
