package pro.verron.officestamper.core;

import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.utils.WmlUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.api.OfficeStamperException.throwing;
import static pro.verron.officestamper.utils.WmlUtils.getFirstParentWithClass;

/// Represents a wrapper for managing and manipulating DOCX paragraph elements. This class provides methods to
/// manipulate the underlying paragraph content, process placeholders, and interact with runs within the paragraph.
public class StandardParagraph
        implements Paragraph {

    private final DocxPart part;
    private final ContentAccessor contents;
    private final ArrayListWml<Object> p;

    /// Constructs a new instance of the StandardParagraph class.
    ///
    /// @param part the source DocxPart that contains the paragraph content.
    /// @param paragraphContent the list of objects representing the paragraph content.
    /// @param p the P object representing the paragraph's structure.
    private StandardParagraph(DocxPart part, ContentAccessor paragraphContent, ArrayListWml<Object> p) {
        this.part = part;
        this.contents = paragraphContent;
        this.p = p;
    }

    public static StandardParagraph from(DocxPart part, Object parent) {
        return switch (parent) {
            case P p -> from(part, p);
            case CTSdtContentRun contentRun -> from(part, contentRun);
            default -> throw new OfficeStamperException("Unsupported parent type: " + parent.getClass());
        };
    }

    /// Creates a new instance of StandardParagraph using the provided DocxPart and P objects.
    ///
    /// @param source the source DocxPart containing the paragraph.
    /// @param paragraph the P object representing the structure and content of the paragraph.
    ///
    /// @return a new instance of StandardParagraph constructed based on the provided source and paragraph.
    public static StandardParagraph from(DocxPart source, P paragraph) {
        return new StandardParagraph(source, paragraph, (ArrayListWml<Object>) paragraph.getContent());
    }

    /// Creates a new instance of StandardParagraph from the provided DocxPart and CTSdtContentRun objects.
    ///
    /// @param source the source DocxPart containing the paragraph content.
    /// @param paragraph the CTSdtContentRun object representing the content of the paragraph.
    ///
    /// @return a new instance of StandardParagraph constructed based on the provided DocxPart and paragraph.
    public static StandardParagraph from(DocxPart source, CTSdtContentRun paragraph) {
        var parent = (SdtRun) paragraph.getParent();
        var parentParent = (P) parent.getParent();
        return new StandardParagraph(source, paragraph, (ArrayListWml<Object>) parentParent.getContent());
    }

    /// Replaces a set of paragraph elements with new ones within the current paragraph's siblings. Ensures that the
    /// elements to be removed are replaced in the appropriate position.
    ///
    /// @param toRemove the list of paragraph elements to be removed.
    /// @param toAdd the list of paragraph elements to be added.
    ///
    /// @throws OfficeStamperException if the current paragraph object is not found in its siblings.
    @Override
    public void replace(List<P> toRemove, List<P> toAdd) {
        var siblings = siblings();
        int index = siblings.indexOf(p.getParent());
        if (index < 0) throw new OfficeStamperException("Impossible");
        siblings.addAll(index, toAdd);
        siblings.removeAll(toRemove);
    }

    private List<Object> siblings() {
        return this.parent(ContentAccessor.class, 1)
                   .orElseThrow(throwing("This paragraph direct parent is not a classic parent object"))
                   .getContent();
    }

    private <T> Optional<T> parent(Class<T> aClass, int depth) {
        return getFirstParentWithClass((Child) p.getParent(), aClass, depth);
    }

    /// Removes the paragraph represented by the current instance. Delegates the removal process to a utility method
    /// that handles the underlying P object.
    @Override
    public void remove() {
        WmlUtils.remove((Child) p.getParent());
    }

    @Override
    public void replace(String expression, Insert insert) {
        insert.assertSerializable(); // TODO Move the check at instance creation
        var newContents = WmlUtils.replaceExpressionWithRun(() -> p, expression, insert);
        var content = contents.getContent();
        content.clear();
        content.addAll(newContents);
    }

    @Override
    public void replace(Object start, Object end, Insert insert) {
        var content = contents.getContent();
        var fromIndex = content.indexOf(start);
        var toIndex = content.indexOf(end);
        if (fromIndex < 0) {
            var msg = "The start element (%s) is not in the paragraph (%s)";
            throw new OfficeStamperException(msg.formatted(start, this));
        }
        if (toIndex < 0) {
            var msg = "The end element (%s) is not in the paragraph (%s)";
            throw new OfficeStamperException(msg.formatted(end, this));
        }
        if (fromIndex > toIndex) {
            var msg = "The start element (%s) is after the end element (%s)";
            throw new OfficeStamperException(msg.formatted(end, this));
        }
        var expression = extractExpression(start, end);
        var newContents = WmlUtils.replaceExpressionWithRun(() -> p, expression, insert);
        content.clear();
        content.addAll(newContents);
    }

    private String extractExpression(Object from, Object to) {
        var content = contents.getContent();
        var fromIndex = content.indexOf(from);
        var toIndex = content.indexOf(to);
        var subContent = content.subList(fromIndex, toIndex + 1);

        var runs = StandardRun.wrap(() -> p);
        runs.removeIf(run -> !subContent.contains(run.run()));
        return runs.stream()
                   .map(StandardRun::getText)
                   .collect(joining());
    }

    /// Returns the aggregated text over all runs.
    ///
    /// @return the text of all runs.
    @Override
    public String asString() {
        return WmlUtils.asString(contents);
    }

    /// Applies the given consumer to the paragraph represented by the current instance. This method facilitates custom
    /// processing by allowing the client to define specific operations to be performed on the paragraph's internal
    /// structure.
    ///
    /// @param pConsumer the consumer function to apply to the paragraph's structure.
    @Override
    public void apply(Consumer<ContentAccessor> pConsumer) {
        pConsumer.accept(() -> p);
    }

    /// Retrieves the nearest parent of the specified type for the current paragraph. The search is performed starting
    /// from the current paragraph and traversing up to the root, with a default maximum depth of Integer.MAX_VALUE.
    ///
    /// @param aClass the class type of the parent to search for
    /// @param <T> the generic type of the parent
    ///
    /// @return an Optional containing the parent of the specified type if found, or an empty Optional if no parent of
    ///         the given type exists
    @Override
    public <T> Optional<T> parent(Class<T> aClass) {
        return parent(aClass, Integer.MAX_VALUE);
    }

    /// Retrieves the collection of comments associated with the current paragraph.
    ///
    /// @return a collection of [Comments.Comment] objects related to the paragraph.
    @Override
    public Collection<Comments.Comment> getComment() {
        return CommentUtil.getCommentFor(() -> p, part.document());
    }

    /// Returns the string representation of the paragraph. This method delegates to the `asString` method to aggregate
    /// the text content of all runs.
    ///
    /// @return a string containing the combined text content of the paragraph's runs.
    @Override
    public String toString() {
        return asString();
    }
}
