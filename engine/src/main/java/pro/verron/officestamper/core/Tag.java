package pro.verron.officestamper.core;

import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Placeholder;

import java.math.BigInteger;

import static pro.verron.officestamper.utils.WmlUtils.asString;

/// Represents a Tag entity consisting of a DocxPart and a CTSmartTagRun.
/// A Tag provides functionality to manipulate and retrieve information
/// related to smart tags embedded within a WordprocessingML-based document.
/// This class offers methods to create a new Tag instance, remove the tag
/// from its parent content, and retrieve associated elements such as
/// Paragraph and Comment objects. Additionally, a placeholder representation
/// of the tag can be accessed through the appropriate method.
public record Tag(DocxPart docxPart, CTSmartTagRun tag) {

    /// Creates a new Tag instance using the provided DocxPart and CTSmartTagRun.
    ///
    /// @param docxPart the DocxPart instance representing the part of the document associated with the new Tag.
    /// @param tag      the CTSmartTagRun representing the smart tag element in the document.
    ///
    /// @return a new Tag instance initialized with the given DocxPart and CTSmartTagRun.
    public static Tag of(DocxPart docxPart, CTSmartTagRun tag) {
        return new Tag(docxPart, tag);
    }

    /// Removes the current tag from its parent's content list.
    ///
    /// This method locates the parent content accessor of the tag,
    /// retrieves its sibling elements, and removes the tag from
    /// the sibling list, detaching it from its parent content.
    public void remove() {
        var parent = (ContentAccessor) tag.getParent();
        var siblings = parent.getContent();
        siblings.remove(tag);
    }

    /// Retrieves the paragraph associated with the smart tag's parent element.
    ///
    /// @return the Paragraph object representing the parent element of the smart tag
    public Paragraph getParagraph() {
        return StandardParagraph.from(docxPart, (P) tag.getParent());
    }

    /// Converts the current tag entity into a Comment representation.
    ///
    /// This method creates a new Comment instance associated with the
    /// parent paragraph of the smart tag, using its placeholder
    /// representation, and a predefined position value.
    ///
    /// @return a Comment object representing the current tag
    public Comment asComment() {
        return StandardComment.create(docxPart, (P) tag.getParent(), asPlaceholder(), BigInteger.ZERO);
    }

    /// Converts the current tag entity into a raw placeholder representation.
    ///
    /// @return a Placeholder object representing the raw placeholder based on the tag's element.
    public Placeholder asPlaceholder() {
        return Placeholders.raw(asString(tag.getContent()));
    }
}
