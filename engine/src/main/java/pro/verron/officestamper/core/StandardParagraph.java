package pro.verron.officestamper.core;

import org.docx4j.wml.*;
import org.jvnet.jaxb.lang.Child;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.utils.wml.*;
import pro.verron.officestamper.utils.wml.DocxDocument.Part;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.wml.WmlUtils.isTagElement;

/// Represents a wrapper for managing and manipulating DOCX paragraph elements. This class provides methods to
/// manipulate the underlying paragraph content, process placeholders, and interact with runs within the paragraph.
public class StandardParagraph implements pro.verron.officestamper.api.Paragraph {

    private final Part part;
    private final Content content;

    /// Constructs a new instance of the StandardParagraph class.
    ///
    /// @param part    the [Part] that contains the paragraph content.
    /// @param content the list of objects representing the paragraph content.
    private StandardParagraph(Part part, Content content) {
        this.part = part;
        this.content = content;
    }

    /// Creates a new instance of [StandardParagraph] from the provided [Part] and parent object.
    ///
    /// @param part   the source [Part].
    /// @param parent the parent object.
    /// @return a new StandardParagraph instance.
    public static StandardParagraph from(Part part, Object parent) {
        return switch (parent) {
            case P p -> from(part, p);
            case CTSdtContentRun contentRun -> from(part, contentRun);
            case CTSmartTagRun smartTagRun when isTagElement(smartTagRun, "officestamper") ->
                    from(part, smartTagRun.getParent());
            case Parent pa -> from(part, pa);
            default -> throw new OfficeStamperException("Unsupported parent type: " + parent.getClass());
        };
    }

    /// Creates a new instance of StandardParagraph using the provided DocxPart and P objects.
    ///
    /// @param part      the source [Part] containing the paragraph.
    /// @param paragraph the P object representing the structure and content of the paragraph.
    /// @return a new instance of StandardParagraph constructed based on the provided source and paragraph.
    public static StandardParagraph from(Part part, P paragraph) {
        return new StandardParagraph(part, new Content(paragraph));
    }

    /// Creates a new instance of StandardParagraph using the provided DocxPart and P objects.
    ///
    /// @param part the source DocxPart containing the paragraph.
    /// @return a new instance of StandardParagraph constructed based on the provided source and paragraph.
    public static StandardParagraph from(Part part, Content content) {
        return new StandardParagraph(part, content);
    }

    /// Creates a new instance of StandardParagraph from the provided DocxPart and CTSdtContentRun objects.
    ///
    /// @param part      the source DocxPart containing the paragraph content.
    /// @param paragraph the CTSdtContentRun object representing the content of the paragraph.
    /// @return a new instance of StandardParagraph constructed based on the provided DocxPart and paragraph.
    public static StandardParagraph from(Part part, CTSdtContentRun paragraph) {
        return new StandardParagraph(part,
                new Content(((P) ((SdtRun) paragraph.getParent()).getParent()), paragraph.getContent())
        );
    }

    /// Removes the paragraph represented by the current instance. Delegates the removal process to a utility method
    /// that handles the underlying P object.
    @Override
    public void remove() {
        content.remove();
    }

    @Override
    public void replace(String expression, Insert insert) {
        WmlUtils.replaceExpressionWithRun(() -> p, expression, insert.elements(), insert::setRPr);
    }

    @Override
    public void replace(Object start, Object end, Insert insert) {
        var contents = ((ContentAccessor) content.get()).getContent();
        var fromIndex = contents.indexOf(start);
        var toIndex = contents.indexOf(end);
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
        WmlUtils.replaceExpressionWithRun(() -> p.subList(fromIndex, toIndex),
                expression,
                insert.elements(),
                insert::setRPr
        );
    }

    private String extractExpression(Object from, Object to) {
        var contents = ((ContentAccessor) content.get()).getContent();
        var fromIndex = contents.indexOf(from);
        var toIndex = contents.indexOf(to);
        var subContent = contents.subList(fromIndex, toIndex + 1);
        return new DocxIterator(new Content(() -> subContent)).selectClass(R.class)
                .map(WmlUtils::asString)
                .collect(joining());
    }

    /// Returns the aggregated text over all runs.
    ///
    /// @return the text of all runs.
    @Override
    public String asString() {
        return WmlUtils.asString(content.get());
    }

    /// Applies the given consumer to the paragraph represented by the current instance. This method facilitates custom
    /// processing by allowing the client to define specific operations to be performed on the paragraph's internal
    /// structure.
    ///
    /// @param pConsumer the consumer function to apply to the paragraph's structure.
    @Override
    public void apply(Consumer<ContentAccessor> pConsumer) {
        content.apply(pConsumer);
    }

    /// Retrieves the collection of comments associated with the current paragraph.
    ///
    /// @return a collection of [Comments.Comment] objects related to the paragraph.
    @Override
    public Collection<Comments.Comment> getComment() {
        return CommentUtil.getCommentFor(new DocxIterator(content), part.document().getPackage());
    }

    @Override
    public Optional<Table.Row> parentTableRow() {
        return WmlUtils.getFirstParentWithClass((Child) ((Child) content.get()).getParent(),
                Tr.class,
                Integer.MAX_VALUE
        ).map((Tr tr) -> new StandardRow(part, (Tbl) tr.getParent(), tr));
    }

    @Override
    public Optional<Table> parentTable() {
        return WmlUtils.getFirstParentWithClass((Child) ((Child) content.get()).getParent(),
                Tbl.class,
                Integer.MAX_VALUE
        ).map(StandardTable::new);
    }

    /// Returns the string representation of the paragraph. This method delegates to the [#asString()] method to
    /// aggregate
    /// the text content of all runs.
    ///
    /// @return a string containing the combined text content of the paragraph's runs.
    @Override
    public String toString() {
        return asString();
    }

}
