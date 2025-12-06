package pro.verron.officestamper.core;

import org.docx4j.wml.*;
import pro.verron.officestamper.api.Comment;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.Paragraph;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Optional;

import static pro.verron.officestamper.utils.WmlUtils.asString;

/// Represents a Tag entity consisting of a DocxPart and a CTSmartTagRun. A Tag provides functionality to manipulate and
/// retrieve information related to smart tags embedded within a WordprocessingML-based document. This class offers
/// methods to create a new Tag instance, remove the tag from its parent content, and retrieve associated elements such
/// as Paragraph and Comment objects. Additionally, a placeholder representation of the tag can be accessed through the
/// appropriate method.
public record Tag(DocxPart docxPart, CTSmartTagRun tag) {

    /// Creates a new Tag instance using the provided DocxPart and CTSmartTagRun.
    ///
    /// @param docxPart the DocxPart instance representing the part of the document associated with the new
    ///         Tag.
    /// @param tag the CTSmartTagRun representing the smart tag element in the document.
    ///
    /// @return a new Tag instance initialized with the given DocxPart and CTSmartTagRun.
    public static Tag of(DocxPart docxPart, CTSmartTagRun tag) {
        return new Tag(docxPart, tag);
    }

    /// Removes the current tag from its parent's content list.
    ///
    /// This method locates the parent content accessor of the tag, retrieves its sibling elements, and removes the tag
    /// from the sibling list, detaching it from its parent content.
    public void remove() {
        var parent = (ContentAccessor) tag.getParent();
        var siblings = parent.getContent();
        siblings.remove(tag);
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
        return StandardComment.create(docxPart, (ContentAccessor) tag.getParent(), expression(), BigInteger.ZERO);
    }

    public String expression() {
        return asString(tag.getContent());
    }

    /// Replaces the current tag with the provided Insert object in the parent's content list. It sets the Run
    /// Properties [RPr] of the provided Insert object, and then removes the current tag and inserts the elements from
    /// the Insert object at the appropriate position.
    ///
    /// @param insert the Insert object containing elements to replace the current tag. It also provides the
    ///         ability to set Run Properties [RPr] for styling purposes.
    public void replace(Insert insert) {
        var optionalRun = getFirst(tag, R.class);
        optionalRun.ifPresent(firstRun -> insert.setRPr(firstRun.getRPr()));
        var parent = (ContentAccessor) tag.getParent();
        var siblings = parent.getContent();
        var index = siblings.indexOf(tag);
        siblings.remove(index);
        siblings.addAll(index, insert.elements());
    }

    private static <T> Optional<T> getFirst(CTSmartTagRun tagRun, Class<T> clazz) {
        return tagRun.getContent()
                     .stream()
                     .filter(clazz::isInstance)
                     .map(clazz::cast)
                     .findFirst();
    }

    public Optional<String> type() {
        return tag.getSmartTagPr()
                  .getAttr()
                  .stream()
                  .filter(a -> a.getName()
                                .equals("type"))
                  .map(CTAttr::getVal)
                  .findFirst();
    }

    public String getContextKey() {
        var smartTagPr = tag.getSmartTagPr();
        if (smartTagPr == null) return String.valueOf(0);
        var smartTagPrAttr = smartTagPr.getAttr();
        if (smartTagPrAttr == null) return String.valueOf(0);
        for (CTAttr attribute : smartTagPrAttr) {
            if ("context".equals(attribute.getName())) try {
                return String.valueOf(Integer.parseInt(attribute.getVal()));
            } catch (NumberFormatException _) {
                return String.valueOf(0);
            }
        }
        return String.valueOf(0);
    }

    public void setContextKey(String contextKey) {
        var name = "context";

        var smartTagPr = tag.getSmartTagPr();
        if (smartTagPr == null) {
            smartTagPr = new CTSmartTagPr();
            tag.setSmartTagPr(smartTagPr);
        }
        var smartTagPrAttr = smartTagPr.getAttr();
        if (smartTagPrAttr == null) {
            smartTagPrAttr = new ArrayList<>();
            tag.setSmartTagPr(smartTagPr);
        }
        for (CTAttr attribute : smartTagPrAttr) {
            if (name.equals(attribute.getName())) attribute.setVal(contextKey);
        }
        var ctAttr = new CTAttr();
        ctAttr.setName(name);
        ctAttr.setVal(contextKey);
        smartTagPrAttr.add(ctAttr);
    }
}
