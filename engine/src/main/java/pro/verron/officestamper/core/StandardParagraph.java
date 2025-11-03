package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.utils.WmlFactory;
import pro.verron.officestamper.utils.WmlUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.api.OfficeStamperException.throwing;
import static pro.verron.officestamper.utils.WmlUtils.getFirstParentWithClass;

/// Represents a wrapper for managing and manipulating DOCX paragraph elements.
/// This class provides methods to manipulate the underlying paragraph content,
/// process placeholders, and interact with runs within the paragraph.
public class StandardParagraph
        implements Paragraph {

    private static final Random RANDOM = new Random();
    private final DocxPart source;
    private final List<Object> contents;
    private final P p;

    /// Constructs a new instance of the StandardParagraph class.
    ///
    /// @param source           the source DocxPart that contains the paragraph content.
    /// @param paragraphContent the list of objects representing the paragraph content.
    /// @param p                the P object representing the paragraph's structure.
    private StandardParagraph(DocxPart source, List<Object> paragraphContent, P p) {
        this.source = source;
        this.contents = paragraphContent;
        this.p = p;
    }

    /// Creates a new instance of StandardParagraph using the provided DocxPart and P objects.
    ///
    /// @param source    the source DocxPart containing the paragraph.
    /// @param paragraph the P object representing the structure and content of the paragraph.
    ///
    /// @return a new instance of StandardParagraph constructed based on the provided source and paragraph.
    public static StandardParagraph from(DocxPart source, P paragraph) {
        return new StandardParagraph(source, paragraph.getContent(), paragraph);
    }

    /// Creates a new instance of StandardParagraph from the provided DocxPart and CTSdtContentRun objects.
    ///
    /// @param source    the source DocxPart containing the paragraph content.
    /// @param paragraph the CTSdtContentRun object representing the content of the paragraph.
    ///
    /// @return a new instance of StandardParagraph constructed based on the provided DocxPart and paragraph.
    public static StandardParagraph from(DocxPart source, CTSdtContentRun paragraph) {
        var parent = (SdtRun) paragraph.getParent();
        var parentParent = (P) parent.getParent();
        return new StandardParagraph(source, paragraph.getContent(), parentParent);
    }

    /// Creates a new instance of ProcessorContext for the current paragraph.
    /// This method generates a comment for the given placeholder and retrieves the first run from the contents,
    /// which are then used to construct the ProcessorContext.
    ///
    /// @param placeholder the placeholder being processed, used to generate the related comment.
    ///
    /// @return a new ProcessorContext instance containing the paragraph, first run, related comment, and placeholder.
    @Override
    public ProcessorContext processorContext(Placeholder placeholder) {
        var comment = comment(placeholder);
        var firstRun = (R) contents.getFirst();
        return new ProcessorContext(this, firstRun, comment, placeholder);
    }

    /// Replaces a set of paragraph elements with new ones within the current paragraph's siblings.
    /// Ensures that the elements to be removed are replaced in the appropriate position.
    ///
    /// @param toRemove the list of paragraph elements to be removed.
    /// @param toAdd    the list of paragraph elements to be added.
    ///
    /// @throws OfficeStamperException if the current paragraph object is not found in its siblings.
    @Override
    public void replace(List<P> toRemove, List<P> toAdd) {
        var siblings = siblings();
        int index = siblings.indexOf(p);
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
        return getFirstParentWithClass(p, aClass, depth);
    }

    /// Removes the paragraph represented by the current instance.
    /// Delegates the removal process to a utility method that handles the underlying P object.
    @Override
    public void remove() {
        WmlUtils.remove(p);
    }

    /// Retrieves the P object representing the paragraph's structure.
    ///
    /// @return the P object associated with the paragraph.
    ///
    /// @deprecated use the inplace edition methods instead
    @Deprecated(since = "2.6", forRemoval = true)
    @Override
    public P getP() {
        return p;
    }

    /// Replaces the given expression with the replacement object within the paragraph.
    /// The replacement object must be a valid DOCX4J Object.
    ///
    /// @param placeholder the expression to be replaced.
    /// @param replacement the object to replace the expression.
    @Override
    public void replace(Placeholder placeholder, Object replacement) {
        if (!WmlUtils.serializable(replacement))
            throw new AssertionError("The replacement object must be a valid DOCX4J Object");
        switch (replacement) {
            case R run -> replaceWithRun(placeholder, run);
            case Br br -> replaceWithBr(placeholder, br);
            default -> throw new AssertionError("Replacement must be a R or Br, but was a " + replacement.getClass());
        }
    }

    private void replaceWithRun(Placeholder placeholder, R replacement) {
        var newContents = WmlUtils.replaceExpressionWithRun(contents, placeholder.expression(), replacement);
        contents.clear();
        contents.addAll(newContents);
    }

    private void replaceWithBr(Placeholder placeholder, Br br) {
        for (StandardRun run : StandardRun.wrap(contents)) {
            var runContentIterator = run.run()
                                        .getContent()
                                        .listIterator();
            while (runContentIterator.hasNext()) {
                Object element = runContentIterator.next();
                if (element instanceof JAXBElement<?> jaxbElement && !jaxbElement.getName()
                                                                                 .getLocalPart()
                                                                                 .equals("instrText"))
                    element = jaxbElement.getValue();
                if (element instanceof Text text) replaceWithBr(placeholder, br, text, runContentIterator);
            }
        }
    }

    @Override
    public void replace(Object from, Object to, R run) {
        var fromIndex = contents.indexOf(from);
        var toIndex = contents.indexOf(to);
        if (fromIndex < 0) {
            var msg = "The start element (%s) is not in the paragraph (%s)";
            throw new OfficeStamperException(msg.formatted(from, this));
        }
        if (toIndex < 0) {
            var msg = "The end element (%s) is not in the paragraph (%s)";
            throw new OfficeStamperException(msg.formatted(to, this));
        }
        if (fromIndex > toIndex) {
            var msg = "The start element (%s) is after the end element (%s)";
            throw new OfficeStamperException(msg.formatted(to, this));
        }
        var expression = extractExpression(from, to);
        var newContents = WmlUtils.replaceExpressionWithRun(contents, expression, run);
        contents.clear();
        contents.addAll(newContents);
    }

    private static void replaceWithBr(
            Placeholder placeholder,
            Br br,
            Text text,
            ListIterator<Object> runContentIterator
    ) {
        var value = text.getValue();
        runContentIterator.remove();
        var runLinebreakIterator = stream(value.split(placeholder.expression())).iterator();
        while (runLinebreakIterator.hasNext()) {
            var subText = WmlFactory.newText(runLinebreakIterator.next());
            runContentIterator.add(subText);
            if (runLinebreakIterator.hasNext()) runContentIterator.add(br);
        }
    }

    private String extractExpression(Object from, Object to) {
        var fromIndex = contents.indexOf(from);
        var toIndex = contents.indexOf(to);
        var subContent = contents.subList(fromIndex, toIndex + 1);

        var runs = StandardRun.wrap(contents);
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

    /// Applies the given consumer to the paragraph represented by the current instance.
    /// This method facilitates custom processing by allowing the client to define
    /// specific operations to be performed on the paragraph's internal structure.
    ///
    /// @param pConsumer the consumer function to apply to the paragraph's structure.
    @Override
    public void apply(Consumer<P> pConsumer) {
        pConsumer.accept(p);
    }

    /// Retrieves the nearest parent of the specified type for the current paragraph.
    /// The search is performed starting from the current paragraph and traversing
    /// up to the root, with a default maximum depth of Integer.MAX_VALUE.
    ///
    /// @param aClass the class type of the parent to search for
    /// @param <T>    the generic type of the parent
    ///
    /// @return an Optional containing the parent of the specified type if found,
    ///         or an empty Optional if no parent of the given type exists
    @Override
    public <T> Optional<T> parent(Class<T> aClass) {
        return parent(aClass, Integer.MAX_VALUE);
    }

    /// Retrieves the collection of comments associated with the current paragraph.
    ///
    /// @return a collection of [Comments.Comment] objects related to the paragraph.
    @Override
    public Collection<Comments.Comment> getComment() {
        return CommentUtil.getCommentFor(contents, source.document());
    }

    private Comment comment(Placeholder placeholder) {
        var id = new BigInteger(16, RANDOM);
        return StandardComment.create(source.document(), p, placeholder, id);
    }

    /// Returns the string representation of the paragraph.
    /// This method delegates to the `asString` method to aggregate the text content of all runs.
    ///
    /// @return a string containing the combined text content of the paragraph's runs.
    @Override
    public String toString() {
        return asString();
    }
}
