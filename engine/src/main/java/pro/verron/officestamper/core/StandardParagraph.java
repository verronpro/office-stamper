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
    private List<StandardRun> runs;

    /// Constructs a new instance of the StandardParagraph class.
    ///
    /// @param source           the source DocxPart that contains the paragraph content.
    /// @param paragraphContent the list of objects representing the paragraph content.
    /// @param p                the P object representing the paragraph's structure.
    private StandardParagraph(DocxPart source, List<Object> paragraphContent, P p) {
        this.source = source;
        this.contents = paragraphContent;
        this.p = p;
        this.runs = initializeRunList(contents);
    }

    /// Initializes a list of StandardRun objects based on the given list of objects.
    /// Iterates over the provided list of objects, identifies instances of type R,
    /// and constructs StandardRun objects while keeping track of their lengths.
    ///
    /// @param objects the list of objects to be iterated over and processed into StandardRun instances
    ///
    /// @return a list of StandardRun objects created from the given input list
    private static List<StandardRun> initializeRunList(List<Object> objects) {
        var currentLength = 0;
        var runList = new ArrayList<StandardRun>(objects.size());
        for (int i = 0; i < objects.size(); i++) {
            var object = objects.get(i);
            if (object instanceof R run) {
                var currentRun = new StandardRun(currentLength, i, run);
                runList.add(currentRun);
                currentLength += currentRun.length();
            }
        }
        return runList;
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
        assert WmlUtils.serializable(replacement);
        switch (replacement) {
            case R run -> replaceWithRun(placeholder, run);
            case Br br -> replaceWithBr(placeholder, br);
            default -> throw new AssertionError("Replacement must be a R or Br, but was a " + replacement.getClass());
        }
    }

    private void replaceWithRun(Placeholder placeholder, R replacement) {
        var text = asString();
        String full = placeholder.expression();

        int matchStartIndex = text.indexOf(full);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + full.length();
        List<StandardRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        if (singleRun) {
            StandardRun run = affectedRuns.getFirst();

            boolean expressionSpansCompleteRun = full.length() == run.length();
            boolean expressionAtStartOfRun = matchStartIndex == run.startIndex();
            boolean expressionAtEndOfRun = matchEndIndex == run.endIndex();
            boolean expressionWithinRun = matchStartIndex > run.startIndex() && matchEndIndex <= run.endIndex();

            replacement.setRPr(run.getPr());

            if (expressionSpansCompleteRun) {
                contents.set(run.indexInParent(), replacement);
            }
            else if (expressionAtStartOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                contents.add(run.indexInParent(), replacement);
            }
            else if (expressionAtEndOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                contents.add(run.indexInParent() + 1, replacement);
            }
            else if (expressionWithinRun) {
                int startIndex = run.indexOf(full);
                int endIndex = startIndex + full.length();
                var newStartRun = RunUtil.create(run.substring(0, startIndex),
                        run.run()
                           .getRPr());
                var newEndRun = RunUtil.create(run.substring(endIndex),
                        run.run()
                           .getRPr());
                contents.remove(run.indexInParent());
                contents.addAll(run.indexInParent(), List.of(newStartRun, replacement, newEndRun));
            }
        }
        else {
            StandardRun firstRun = affectedRuns.getFirst();
            StandardRun lastRun = affectedRuns.getLast();
            replacement.setRPr(firstRun.getPr());
            removeExpression(firstRun, matchStartIndex, matchEndIndex, lastRun, affectedRuns);
            // add replacement run between first and last run
            contents.add(firstRun.indexInParent() + 1, replacement);
        }
        this.runs = initializeRunList(contents);
    }

    private void replaceWithBr(Placeholder placeholder, Br br) {
        for (StandardRun standardRun : runs) {
            var runContentIterator = standardRun.run()
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

    private List<StandardRun> getAffectedRuns(int startIndex, int endIndex) {
        return runs.stream()
                   .filter(run -> run.isTouchedByRange(startIndex, endIndex))
                   .toList();
    }

    private void removeExpression(
            StandardRun firstRun,
            int matchStartIndex,
            int matchEndIndex,
            StandardRun lastRun,
            List<StandardRun> affectedRuns
    ) {
        // remove the expression from the first run
        firstRun.replace(matchStartIndex, matchEndIndex, "");
        // remove all runs between first and last
        for (StandardRun run : affectedRuns) {
            if (!Objects.equals(run, firstRun) && !Objects.equals(run, lastRun)) {
                contents.remove(run.run());
            }
        }
        // remove the expression from the last run
        lastRun.replace(matchStartIndex, matchEndIndex, "");
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

    /// Returns the aggregated text over all runs.
    ///
    /// @return the text of all runs.
    @Override
    public String asString() {
        return runs.stream()
                   .map(StandardRun::getText)
                   .collect(joining());
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

    /// Represents a run (i.e., a text fragment) in a paragraph. The run is indexed relative to the containing paragraph
    /// and also relative to the containing document.
    ///
    /// @param startIndex    the start index of the run relative to the containing paragraph.
    /// @param indexInParent the index of the run relative to the containing document.
    /// @param run           the run itself.
    ///
    /// @author Joseph Verron
    /// @author Tom Hombergs
    /// @version ${version}
    /// @since 1.0.0
    public record StandardRun(int startIndex, int indexInParent, R run) {

        /// Retrieves a substring from the text content of this run, starting at the specified begin index.
        ///
        /// @param beginIndex the beginning index, inclusive, for the substring.
        ///
        /// @return the substring of the run's text starting from the specified begin index to the end of the text.
        public String substring(int beginIndex) {
            return getText().substring(beginIndex);
        }

        /// Retrieves a substring from the text content of this run, starting
        /// at the specified begin index and ending at the specified end index.
        ///
        /// @param beginIndex the beginning index, inclusive, for the substring.
        /// @param endIndex   the ending index, exclusive, for the substring.
        ///
        /// @return the substring of the run's text from the specified begin index to the specified end index.
        public String substring(int beginIndex, int endIndex) {
            return getText().substring(beginIndex, endIndex);
        }

        /// Finds the index of the first occurrence of the specified substring in the text of the current run.
        ///
        /// @param full the substring to search for within the run's text.
        ///
        /// @return the index of the first occurrence of the specified substring,
        /// or &ndash;1 if the substring is not found.
        public int indexOf(String full) {
            return getText().indexOf(full);
        }

        /// Returns the text string of a run.
        ///
        /// @return [String] representation of the run.
        public String getText() {
            return RunUtil.getText(run);
        }

        /// Retrieves the properties associated with this run.
        ///
        /// @return the [RPr] object representing the properties of the run.
        public RPr getPr() {
            return run.getRPr();
        }

        /// Determines whether the current run is affected by the specified range of global start and end indices.
        /// A run is considered "touched" if any part of it overlaps with the given range.
        ///
        /// @param globalStartIndex the global start index of the range.
        /// @param globalEndIndex   the global end index of the range.
        ///
        /// @return `true` if the current run is touched by the specified range; `false` otherwise.
        public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
            var startsInRange = (globalStartIndex < startIndex) && (startIndex <= globalEndIndex);
            var endsInRange = (globalStartIndex < endIndex()) && (endIndex() <= globalEndIndex);
            var rangeFullyContainsRun = (startIndex <= globalStartIndex) && (globalEndIndex <= endIndex());
            return startsInRange || endsInRange || rangeFullyContainsRun;
        }

        /// Calculates the end index of the current run based on its start index and length.
        ///
        /// @return the end index of the run.
        public int endIndex() {
            return startIndex + length();
        }

        /// Calculates the length of the text content of this run.
        ///
        /// @return the length of the text in the current run.
        public int length() {
            return getText().length();
        }

        /// Replaces the substring starting at the given index with the given replacement string.
        ///
        /// @param globalStartIndex the global index at which to start the replacement.
        /// @param globalEndIndex   the global index at which to end the replacement.
        /// @param replacement      the string to replace the substring at the specified global index.
        public void replace(int globalStartIndex, int globalEndIndex, String replacement) {
            int localStartIndex = globalIndexToLocalIndex(globalStartIndex);
            int localEndIndex = globalIndexToLocalIndex(globalEndIndex);
            var text = substring(0, localStartIndex);
            text += replacement;
            String runText = getText();
            if (!runText.isEmpty()) {
                text += substring(localEndIndex);
            }
            RunUtil.setText(run, text);
        }

        /// Converts a global index to a local index within the context of this run.
        /// (meaning the index relative to multiple aggregated runs)
        ///
        /// @param globalIndex the global index to convert.
        ///
        /// @return the local index corresponding to the given global index.
        private int globalIndexToLocalIndex(int globalIndex) {
            if (globalIndex < startIndex) return 0;
            else if (globalIndex > endIndex()) return length();
            else return globalIndex - startIndex;
        }
    }
}
