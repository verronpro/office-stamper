package pro.verron.officestamper.utils.wml;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TraversalUtil;
import org.docx4j.finders.CommentFinder;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.vml.CTShadow;
import org.docx4j.vml.CTTextbox;
import org.docx4j.vml.VmlShapeElements;
import org.docx4j.wml.*;
import org.docx4j.wml.Comments.Comment;
import org.jspecify.annotations.Nullable;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.openpackaging.OpenpackagingFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.wml.WmlFactory.*;

/// Utility class with methods to help in the interaction with [WordprocessingMLPackage] documents and their elements,
/// such as comments, parents, and child elements.
public final class WmlUtils {

    private static final String PRESERVE = "preserve";
    private static final Logger log = LoggerFactory.getLogger(WmlUtils.class);

    private WmlUtils() {
        throw new UtilsException("Utility class shouldn't be instantiated");
    }

    /// Attempts to find the first parent of a given child element that is an instance of the specified class within the
    /// defined search depth.
    ///
    /// @param child the [Child] element from which the search for a parent begins.
    /// @param clazz the [Class] type to match for the parent
    /// @param depth the maximum amount levels to traverse up the parent hierarchy
    /// @param <T> the type of the parent class to search for
    ///
    /// @return an [Optional] containing the first parent matching the specified class, or an empty [Optional] if no
    ///         match found.
    public static <T> Optional<T> getFirstParentWithClass(Child child, Class<T> clazz, int depth) {
        var parent = child.getParent();
        var currentDepth = 0;
        while (currentDepth <= depth) {
            currentDepth++;
            if (parent == null) return Optional.empty();
            if (clazz.isInstance(parent)) return Optional.of(clazz.cast(parent));
            if (parent instanceof Child next) parent = next.getParent();
        }
        return Optional.empty();
    }

    /// Extracts a list of comment elements from the specified [WordprocessingMLPackage] document.
    ///
    /// @param document the [WordprocessingMLPackage] document from which to extract comment elements
    ///
    /// @return a list of [Child] objects representing the extracted comment elements
    public static List<Child> extractCommentElements(WordprocessingMLPackage document) {
        var commentFinder = new CommentFinder();
        TraversalUtil.visit(document, true, commentFinder);
        return commentFinder.getCommentElements();
    }

    /// Finds a comment with the given ID in the specified [WordprocessingMLPackage] document.
    ///
    /// @param document the [WordprocessingMLPackage] document to search for the comment
    /// @param id the ID of the comment to find
    ///
    /// @return an [Optional] containing the [Comment] if found, or an empty [Optional] if not found.
    public static Optional<Comment> findComment(WordprocessingMLPackage document, BigInteger id) {
        var name = OpenpackagingFactory.newPartName("/word/comments.xml");
        var parts = document.getParts();
        var wordComments = (CommentsPart) parts.get(name);
        var comments = getComments(wordComments);
        return comments.getComment()
                       .stream()
                       .filter(idEqual(id))
                       .findFirst();
    }

    private static Comments getComments(CommentsPart wordComments) {
        try {
            return wordComments.getContents();
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }

    private static Predicate<Comment> idEqual(BigInteger id) {
        return comment -> {
            var commentId = comment.getId();
            return commentId.equals(id);
        };
    }


    /// Removes the specified child element from its parent container. Depending on the type of the parent element, the
    /// removal process is delegated to the appropriate helper method. If the child is contained within a table cell and
    /// the cell is empty after removal, an empty paragraph is added to the cell.
    ///
    /// @param child the [Child] element to be removed
    ///
    /// @throws UtilsException if the parent of the child element is of an unexpected type
    public static void remove(Child child) {
        switch (child.getParent()) {
            case ContentAccessor parent -> remove(parent, child);
            case CTFootnotes parent -> remove(parent, child);
            case CTEndnotes parent -> remove(parent, child);
            case SdtRun parent -> remove(parent, child);
            default -> throw new UtilsException("Unexpected value: " + child.getParent());
        }
        if (child.getParent() instanceof Tc cell) ensureValidity(cell);
    }

    private static void remove(ContentAccessor parent, Child child) {
        var siblings = parent.getContent();
        var iterator = siblings.listIterator();
        while (iterator.hasNext()) {
            if (equals(iterator.next(), child)) {
                iterator.remove();
                break;
            }
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private static void remove(CTFootnotes parent, Child child) {
        parent.getFootnote()
              .remove(child);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private static void remove(CTEndnotes parent, Child child) {
        parent.getEndnote()
              .remove(child);
    }

    private static void remove(SdtRun parent, Child child) {
        parent.getSdtContent()
              .getContent()
              .remove(child);
    }

    /// Utility method to ensure the validity of a table cell by adding an empty paragraph if necessary.
    ///
    /// @param cell the [Tc] to be checked and updated.
    public static void ensureValidity(Tc cell) {
        if (!containsAnElementOfAnyClasses(cell.getContent(), P.class, Tbl.class)) {
            addEmptyParagraph(cell);
        }
    }

    private static boolean equals(Object o1, Object o2) {
        if (o1 instanceof JAXBElement<?> e1) o1 = e1.getValue();
        if (o2 instanceof JAXBElement<?> e2) o2 = e2.getValue();
        return Objects.equals(o1, o2);
    }

    private static boolean containsAnElementOfAnyClasses(Collection<Object> collection, Class<?>... classes) {
        return collection.stream()
                         .anyMatch(element -> isAnElementOfAnyClasses(element, classes));
    }

    private static void addEmptyParagraph(Tc cell) {
        var emptyParagraph = WmlFactory.newParagraph();
        var cellContent = cell.getContent();
        cellContent.add(emptyParagraph);
    }

    private static boolean isAnElementOfAnyClasses(Object element, Class<?>... classes) {
        for (var clazz : classes) {
            if (clazz.isInstance(unwrapJAXBElement(element))) return true;
        }
        return false;
    }

    private static Object unwrapJAXBElement(Object element) {
        return element instanceof JAXBElement<?> jaxbElement ? jaxbElement.getValue() : element;
    }

    /// Extracts textual content from a given object, handling various object types, such as runs, text elements, and
    /// other specific constructs. The method accounts for different cases, such as run breaks, hyphens, and other
    /// document-specific constructs, and converts them into corresponding string representations.
    ///
    /// @param content the object from which text content is to be extracted. This could be of various types
    ///         such as [R], [JAXBElement], [Text] or specific document elements.
    ///
    /// @return a string representation of the extracted textual content. If the object's type is not handled, an empty
    ///         string is returned.
    public static String asString(Object content) {
        return switch (content) {
            case P paragraph -> asString(paragraph.getContent());
            case R run -> asString(run.getContent());
            case JAXBElement<?> jaxbElement when jaxbElement.getName()
                                                            .getLocalPart()
                                                            .equals("instrText") -> "<instrText>";
            case JAXBElement<?> jaxbElement when !jaxbElement.getName()
                                                             .getLocalPart()
                                                             .equals("instrText") -> asString(jaxbElement.getValue());
            case Text text -> asString(text);
            case R.Tab _ -> "\t";
            case R.Cr _ -> "\n";
            case Br br when br.getType() == null -> "\n";
            case Br br when br.getType() == STBrType.PAGE -> "\n";
            case Br br when br.getType() == STBrType.COLUMN -> "\n";
            case Br br when br.getType() == STBrType.TEXT_WRAPPING -> "\n";

            case R.NoBreakHyphen _ -> "‑";
            case R.SoftHyphen _ -> "\u00AD";
            case R.LastRenderedPageBreak _, R.AnnotationRef _, R.CommentReference _, Drawing _ -> "";
            case FldChar _ -> "<fldchar>";
            case CTFtnEdnRef ref -> "<ref(%s)>".formatted(ref.getId());
            case R.Sym sym -> "<sym(%s, %s)>".formatted(sym.getFont(), sym.getChar());
            case List<?> list -> list.stream()
                                     .map(WmlUtils::asString)
                                     .collect(joining());
            case ProofErr _, CTShadow _ -> "";
            case SdtRun sdtRun -> asString(sdtRun.getSdtContent());
            case ContentAccessor contentAccessor -> asString(contentAccessor.getContent());
            case Pict pict -> asString(pict.getAnyAndAny());
            case VmlShapeElements vmlShapeElements -> asString(vmlShapeElements.getEGShapeElements());
            case CTTextbox textbox -> asString(textbox.getTxbxContent());
            case CommentRangeStart _, CommentRangeEnd _ -> "";
            default -> {
                log.debug("Unhandled object type: {}", content.getClass());
                yield "";
            }
        };
    }

    private static String asString(Text text) {
        // According to specs, 'space' value can be empty or 'preserve'.
        // In the first case, we are supposed to ignore spaces around the 'text' value.
        var value = text.getValue();
        var space = text.getSpace();
        return Objects.equals(space, PRESERVE) ? value : value.trim();
    }

    /// Inserts a smart tag with the specified element type into the given paragraph at the position of the expression.
    ///
    /// @param element the element type for the smart tag
    /// @param paragraph the [P] paragraph to insert the smart tag into
    /// @param expression the expression to replace with the smart tag
    /// @param start the start index of the expression
    /// @param end the end index of the expression
    ///
    /// @return a list of [Object] representing the updated content
    public static List<Object> insertSmartTag(String element, P paragraph, String expression, int start, int end) {
        var run = newRun(expression);
        var smartTag = newSmartTag("officestamper", run, newCtAttr("type", element));
        findFirstAffectedRunPr(paragraph, start, end).ifPresent(run::setRPr);
        return replace(paragraph, List.of(smartTag), start, end);
    }

    /// Finds the first affected run properties within the specified range.
    ///
    /// @param contentAccessor the [ContentAccessor] to search in
    /// @param start the start index of the range
    /// @param end the end index of the range
    ///
    /// @return an [Optional] containing the [RPr] if found, or an empty [Optional] if not found
    public static Optional<RPr> findFirstAffectedRunPr(ContentAccessor contentAccessor, int start, int end) {
        var iterator = new DocxIterator(contentAccessor).selectClass(R.class);
        var runs = StandardRun.wrap(iterator);

        var affectedRuns = runs.stream()
                               .filter(run -> run.isTouchedByRange(start, end))
                               .toList();

        var firstRun = affectedRuns.getFirst();
        var firstRunPr = firstRun.getPr();
        return Optional.ofNullable(firstRunPr);
    }

    /// Replaces content within the specified range with the provided insert objects.
    ///
    /// @param contentAccessor the [ContentAccessor] in which to replace content
    /// @param insert the list of objects to insert
    /// @param startIndex the start index of the range to replace
    /// @param endIndex the end index of the range to replace
    ///
    /// @return a list of [Object] representing the updated content
    public static List<Object> replace(
            ContentAccessor contentAccessor,
            List<Object> insert,
            int startIndex,
            int endIndex
    ) {
        var iterator = new DocxIterator(contentAccessor).selectClass(R.class);
        var runs = StandardRun.wrap(iterator);
        var affectedRuns = runs.stream()
                               .filter(run -> run.isTouchedByRange(startIndex, endIndex))
                               .toList();

        var firstRun = affectedRuns.getFirst();
        var firstR = firstRun.run();
        var firstSiblings = ((ContentAccessor) firstR.getParent()).getContent();
        var firstIndex = firstSiblings.indexOf(firstRun.run());

        boolean singleRun = affectedRuns.size() == 1;
        if (singleRun) {
            boolean expressionSpansCompleteRun = endIndex - startIndex == firstRun.length();
            boolean expressionAtStartOfRun = startIndex == firstRun.startIndex();
            boolean expressionAtEndOfRun = endIndex == firstRun.endIndex();
            boolean expressionWithinRun = startIndex > firstRun.startIndex() && endIndex <= firstRun.endIndex();

            if (expressionSpansCompleteRun) {
                firstRun.replace(startIndex, endIndex, "");
                firstSiblings.addAll(firstIndex, insert);
            }
            else if (expressionAtStartOfRun) {
                firstRun.replace(startIndex, endIndex, "");
                firstSiblings.addAll(firstIndex, insert);
            }
            else if (expressionAtEndOfRun) {
                firstRun.replace(startIndex, endIndex, "");
                firstSiblings.addAll(firstIndex + 1, insert);
            }
            else if (expressionWithinRun) {
                var originalRun = firstRun.run();
                var originalRPr = originalRun.getRPr();
                var newStartRun = create(firstRun.left(startIndex), originalRPr);
                var newEndRun = create(firstRun.right(endIndex), originalRPr);
                firstSiblings.remove(firstIndex);
                firstSiblings.addAll(firstIndex, wrap(newStartRun, insert, newEndRun));
            }
        }
        else {
            StandardRun lastRun = affectedRuns.getLast();
            removeExpression(firstSiblings, firstRun, startIndex, endIndex, lastRun, affectedRuns);
            // add replacement run between first and last run
            firstSiblings.addAll(firstIndex + 1, insert);
        }
        return new ArrayList<>(contentAccessor.getContent());
    }

    /// Creates a new run with the specified text, and the specified run style.
    ///
    /// @param text the initial text of the [R].
    /// @param rPr the [RPr] to apply to the run
    ///
    /// @return the newly created [R].
    public static R create(String text, RPr rPr) {
        R newStartRun = newRun(text);
        newStartRun.setRPr(rPr);
        return newStartRun;
    }

    private static Collection<?> wrap(R prefix, Collection<?> elements, R suffix) {
        var merge = new ArrayList<>();
        merge.add(prefix);
        merge.addAll(elements);
        merge.add(suffix);
        return merge;
    }

    private static void removeExpression(
            List<Object> contents,
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

    /// Creates a new run with the specified text and inherits the style of the parent paragraph.
    ///
    /// @param text the initial text of the [R].
    /// @param paragraphPr the [PPr] to apply to the run
    ///
    /// @return the newly created [R].
    public static R create(String text, PPr paragraphPr) {
        R run = newRun(text);
        applyParagraphStyle(run, paragraphPr);
        return run;
    }

    /// Applies the style of the given paragraph to the given content object (if the content object is a [R]).
    ///
    /// @param run the [R] to which the style should be applied.
    /// @param paragraphPr the [PPr] containing the style to apply
    public static void applyParagraphStyle(R run, @Nullable PPr paragraphPr) {
        if (paragraphPr == null) return;
        var runPr = paragraphPr.getRPr();
        if (runPr == null) return;
        RPr runProperties = new RPr();
        StyleUtil.apply(runPr, runProperties);
        run.setRPr(runProperties);
    }

    /// Sets the text of the given run to the given value.
    ///
    /// @param run the [R] whose text to change.
    /// @param text the text to set.
    public static void setText(R run, String text) {
        run.getContent()
           .clear();
        Text textObj = newText(text);
        run.getContent()
           .add(textObj);
    }

    /// Replaces all occurrences of the specified expression with the provided run objects.
    ///
    /// @param contentAccessor the [ContentAccessor] in which to replace the expression
    /// @param expression the expression to replace
    /// @param insert the list of objects to insert
    /// @param onRPr a consumer to handle [RPr] properties
    ///
    /// @return a list of [Object] representing the updated content
    public static List<Object> replaceExpressionWithRun(
            ContentAccessor contentAccessor,
            String expression,
            List<Object> insert,
            Consumer<RPr> onRPr
    ) {
        var text = asString(contentAccessor);
        int matchStartIndex = text.indexOf(expression);
        if (matchStartIndex == -1) /*nothing to replace*/ return contentAccessor.getContent();
        int matchEndIndex = matchStartIndex + expression.length();
        findFirstAffectedRunPr(contentAccessor, matchStartIndex, matchEndIndex).ifPresent(onRPr);
        return replace(contentAccessor, insert, matchStartIndex, matchEndIndex);
    }

    /// @param startIndex the start index of the run relative to the containing paragraph.
    /// @param run the [R] run itself.
    private record StandardRun(int startIndex, R run) {

        /// Initializes a list of [StandardRun] objects based on the given iterator of [R] objects.
        ///
        /// @param iterator the iterator of [R] objects to be processed into [StandardRun] instances
        ///
        /// @return a list of [StandardRun] objects created from the given iterator
        public static List<StandardRun> wrap(Iterator<R> iterator) {
            var index = 0;
            var runList = new ArrayList<StandardRun>();
            while (iterator.hasNext()) {
                var run = iterator.next();
                var currentRun = new StandardRun(index, run);
                runList.add(currentRun);
                index += currentRun.length();
            }
            return runList;
        }

        /// Calculates the length of the text content of this run.
        ///
        /// @return the length of the text in the current run.
        public int length() {
            return getText().length();
        }

        /// Returns the text string of a run.
        ///
        /// @return [String] representation of the run.
        public String getText() {
            return asString(run);
        }

        /// Retrieves the properties associated with this run.
        ///
        /// @return the [RPr] object representing the properties of the run.
        public RPr getPr() {
            return run.getRPr();
        }

        /// Determines whether the current run is affected by the specified range of global start and end indices. A run
        /// is considered "touched" if any part of it overlaps with the given range.
        ///
        /// @param globalStartIndex the global start index of the range.
        /// @param globalEndIndex the global end index of the range.
        ///
        /// @return `true` if the current run is touched by the specified range; `false` otherwise.
        public boolean isTouchedByRange(int globalStartIndex, int globalEndIndex) {
            return startsInRange(globalStartIndex, globalEndIndex) || endsInRange(globalStartIndex, globalEndIndex)
                   || englobesRange(globalStartIndex, globalEndIndex);
        }

        private boolean startsInRange(int globalStartIndex, int globalEndIndex) {
            return globalStartIndex < startIndex && startIndex <= globalEndIndex;
        }

        private boolean endsInRange(int globalStartIndex, int globalEndIndex) {
            return globalStartIndex < endIndex() && endIndex() <= globalEndIndex;
        }

        private boolean englobesRange(int globalStartIndex, int globalEndIndex) {
            return startIndex <= globalStartIndex && globalEndIndex <= endIndex();
        }

        /// Calculates the end index of the current run based on its start index and length.
        ///
        /// @return the end index of the run.
        public int endIndex() {
            return startIndex + length();
        }

        /// Replaces the substring starting at the given index with the given replacement string.
        ///
        /// @param globalStartIndex the global index at which to start the replacement.
        /// @param globalEndIndex the global index at which to end the replacement.
        /// @param replacement the string to replace the substring at the specified global index.
        public void replace(int globalStartIndex, int globalEndIndex, String replacement) {
            var text = left(globalStartIndex) + replacement + right(globalEndIndex);
            setText(run, text);
        }

        /// Extracts a substring of the run's text, starting from the beginning and extending up to the localized index
        /// of the specified global end index.
        ///
        /// @param globalEndIndex the global end index used to determine the cutoff point for the extracted
        ///         substring.
        ///
        /// @return a substring of the run's text, starting at the beginning and ending at the specified localized
        ///         index.
        public String left(int globalEndIndex) {
            return getText().substring(0, localize(globalEndIndex));
        }

        /// Extracts a substring of the run's text, starting from the localized index of the specified global start
        /// index to the end of the run's text.
        ///
        /// @param globalStartIndex the global index specifying the starting point for the substring in the
        ///         run's text.
        ///
        /// @return a substring of the run's text starting from the localized index corresponding to the provided global
        ///         start index.
        public String right(int globalStartIndex) {
            return getText().substring(localize(globalStartIndex));
        }

        /// Converts a global index to a local index within the context of this run. (meaning the index relative to
        /// multiple aggregated runs)
        ///
        /// @param globalIndex the global index to convert.
        ///
        /// @return the local index corresponding to the given global index.
        private int localize(int globalIndex) {
            if (globalIndex < startIndex) return 0;
            else if (globalIndex > endIndex()) return length();
            else return globalIndex - startIndex;
        }

        /// Gets the start index of this run.
        ///
        /// @return the start index of the run relative to the containing paragraph.
        @Override
        public int startIndex() {return startIndex;}

        /// Gets the underlying run object.
        ///
        /// @return the [R] run object.
        @Override
        public R run() {return run;}
    }
}
