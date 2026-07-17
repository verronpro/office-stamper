package pro.verron.officestamper.core;

import org.docx4j.wml.CTAttr;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.utils.wml.SmartTag;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static pro.verron.officestamper.utils.wml.WmlUtils.asString;

/// Represents a Tag entity consisting of a DocxPart and a CTSmartTagRun. A Tag provides functionality to manipulate and
/// retrieve information related to smart tags embedded within a WordprocessingML-based document. This class offers
/// methods to create a new Tag instance, remove the tag from its parent content, and retrieve associated elements such
/// as Paragraph and Comment objects. Additionally, a placeholder representation of the tag can be accessed through the
/// appropriate method.
///
/// @param docxPart the DocxPart instance representing the part of the document associated with the tag.
/// @param tag      the CTSmartTagRun representing the smart tag element in the document.
public record Tag(DocxPart docxPart, SmartTag tag) {

    /// Creates a new Tag instance using the provided DocxPart and CTSmartTagRun.
    ///
    /// @param docxPart the DocxPart instance representing the part of the document associated with the new Tag.
    /// @param tag      the CTSmartTagRun representing the smart tag element in the document.
    /// @return a new Tag instance initialized with the given DocxPart and CTSmartTagRun.
    public static Tag of(DocxPart docxPart, SmartTag tag) {
        return new Tag(docxPart, tag);
    }

    private static <T> Optional<T> getFirst(List<Object> content, Class<T> clazz) {
        return content.stream().filter(clazz::isInstance).map(clazz::cast).findFirst();
    }

    /// Removes the current tag from its parent's content list.
    ///
    /// This method locates the parent content accessor of the tag, retrieves its sibling elements, and removes the tag
    /// from the sibling list, detaching it from its parent content.
    public void remove() {
        tag.remove();
    }

    /// Retrieves the paragraph associated with the smart tag's parent element.
    ///
    /// @return the Paragraph object representing the parent element of the smart tag
    public Paragraph getParagraph() {
        return StandardParagraph.from(docxPart, tag.getParent());
    }

    /// Converts the current tag entity into a Comment representation.
    ///
    /// This method creates a new Comment instance associated with the parent paragraph of the smart tag, using its
    /// placeholder representation, and a predefined position value.
    ///
    /// @return a Comment object representing the current tag
    public Comment asComment() {
        return StandardComment.create(docxPart, tag.getParent(), expression(), BigInteger.ZERO);
    }

    /// Retrieves the expression of the tag.
    ///
    /// @return the expression.
    public String expression() {
        return asString(tag.getContent());
    }

    /// Replaces the current tag with the provided Insert object in the parent's content list. It sets the Run
    /// Properties [RPr] of the provided Insert object and then removes the current tag and inserts the elements from
    /// the Insert object at the appropriate position.
    ///
    /// @param insert the Insert object containing elements to replace the current tag. It also provides the
    ///         ability to set Run Properties [RPr] for styling purposes.
    public void replace(Insert insert) {
        var optionalRun = getFirst(tag.getContent(), R.class);
        optionalRun.ifPresent(firstRun -> insert.setRPr(firstRun.getRPr()));
        var siblings = tag.getContent();
        siblings.clear();
        siblings.addAll(insert.elements());
    }

    /// Retrieves the type of the tag.
    ///
    /// @return the type.
    public Optional<String> type() {
        return tag.getProperty("type");
    }

    /// Retrieves the context key of the tag.
    ///
    /// @return the context key.
    public String getContextKey() { //TODO: replace by an int value, instead of string type
        return tag.getProperty("context").orElse("0");
    }
}
