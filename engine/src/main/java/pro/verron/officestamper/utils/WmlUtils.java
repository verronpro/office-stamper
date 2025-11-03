package pro.verron.officestamper.utils;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.CommentFinder;
import org.docx4j.model.styles.StyleUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.utils.WmlFactory.newRun;
import static pro.verron.officestamper.utils.WmlFactory.newText;

/// Utility class with methods to help in the interaction with WordprocessingMLPackage documents
/// and their elements, such as comments, parents, and child elements.
public final class WmlUtils {

    private static final String PRESERVE = "preserve";
    private static final Logger log = LoggerFactory.getLogger(WmlUtils.class);

    private WmlUtils() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /// Attempts to find the first parent of a given child element that is an instance of the specified class within
    /// the defined search depth.
    ///
    /// @param child the child element from which the search for a parent begins.
    /// @param clazz the class type to match for the parent
    /// @param depth the maximum amount levels to traverse up the parent hierarchy
    /// @param <T>   the type of the parent class to search for
    ///
    /// @return an Optional containing the first parent matching the specified class, or an empty Optional if no match
    /// found.
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

    /// Extracts a list of comment elements from the specified WordprocessingMLPackage document.
    ///
    /// @param document the WordprocessingMLPackage document from which to extract comment elements
    ///
    /// @return a list of Child objects representing the extracted comment elements
    public static List<Child> extractCommentElements(WordprocessingMLPackage document) {
        var commentFinder = new CommentFinder();
        TraversalUtil.visit(document, true, commentFinder);
        return commentFinder.getCommentElements();
    }

    /// Finds a comment with the given ID in the specified WordprocessingMLPackage document.
    ///
    /// @param document the WordprocessingMLPackage document to search for the comment
    /// @param id       the ID of the comment to find
    ///
    /// @return an Optional containing the Comment if found, or an empty Optional if not found.
    public static Optional<Comments.Comment> findComment(WordprocessingMLPackage document, BigInteger id) {
        var name = getPartName("/word/comments.xml");
        var parts = document.getParts();
        var wordComments = (CommentsPart) parts.get(name);
        var comments = getComments(wordComments);
        return comments.getComment()
                       .stream()
                       .filter(idEqual(id))
                       .findFirst();
    }

    private static PartName getPartName(String partName) {
        try {
            return new PartName(partName);
        } catch (InvalidFormatException e) {
            throw new OfficeStamperException(e);
        }
    }

    private static Comments getComments(CommentsPart wordComments) {
        try {
            return wordComments.getContents();
        } catch (Docx4JException e) {
            throw new OfficeStamperException(e);
        }
    }

    private static Predicate<Comments.Comment> idEqual(BigInteger id) {
        return comment -> {
            var commentId = comment.getId();
            return commentId.equals(id);
        };
    }


    /// Removes the specified child element from its parent container.
    /// Depending on the type of the parent element, the removal process
    /// is delegated to the appropriate helper method. If the child is
    /// contained within a table cell and the cell is empty after removal,
    /// an empty paragraph is added to the cell.
    ///
    /// @param child the child element to be removed
    ///
    /// @throws OfficeStamperException if the parent of the child element is of an unexpected type
    public static void remove(Child child) {
        switch (child.getParent()) {
            case ContentAccessor parent -> remove(parent, child);
            case CTFootnotes parent -> remove(parent, child);
            case CTEndnotes parent -> remove(parent, child);
            case SdtRun parent -> remove(parent, child);
            default -> throw new OfficeStamperException("Unexpected value: " + child.getParent());
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
    /// @param cell the table cell to be checked and updated.
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
        for (Object element : collection) {
            if (isAnElementOfAnyClasses(element, classes)) {
                return true;
            }
        }
        return false;
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

    /// Checks if the given object is serializable to XML.
    ///
    /// @param object the object to be checked for XML serialization
    ///
    /// @return true if the object can be serialized to XML, false otherwise
    public static boolean serializable(Object object) {
        try {
            XmlUtils.marshaltoString(object);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    /// Extracts textual content from a given object, handling various object types,
    /// such as runs, text elements, and other specific constructs.
    /// The method accounts for different cases, such as run breaks, hyphens,
    /// and other document-specific constructs, and converts them into
    /// corresponding string representations.
    ///
    /// @param content the object from which text content is to be extracted.
    /// This could be of various types such as R, JAXBElement, Text or specific document elements.
    ///
    /// @return a string representation of the extracted textual content.
    /// If the object's type is not handled, an empty string is returned.
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

    public static void addSmartTag(P paragraph, int start, int end) {
        List<Object> prefix = paragraph.getContent()
                                       .subList(0, start);
        List<Object> select = paragraph.getContent()
                                       .subList(start, end);
        List<Object> suffix = paragraph.getContent()
                                       .subList(end,
                                               paragraph.getContent()
                                                        .size());
    }

    public static List<Object> replace(
            List<Object> contents,
            String full,
            R replacement,
            int matchStartIndex,
            int matchEndIndex
    ) {
        var runs = wrap(contents);
        var affectedRuns = runs.stream()
                               .filter(run -> run.isTouchedByRange(matchStartIndex, matchEndIndex))
                               .toList();

        boolean singleRun = affectedRuns.size() == 1;

        if (singleRun) {
            Run run = affectedRuns.getFirst();

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
                var originalRun = run.run();
                var originalRPr = originalRun.getRPr();
                var newStartRun = create(run.substring(0, startIndex), originalRPr);
                var newEndRun = create(run.substring(endIndex), originalRPr);
                contents.remove(run.indexInParent());
                contents.addAll(run.indexInParent(), List.of(newStartRun, replacement, newEndRun));
            }
        }
        else {
            Run firstRun = affectedRuns.getFirst();
            Run lastRun = affectedRuns.getLast();
            replacement.setRPr(firstRun.getPr());
            removeExpression(contents, firstRun, matchStartIndex, matchEndIndex, lastRun, affectedRuns);
            // add replacement run between first and last run
            contents.add(firstRun.indexInParent() + 1, replacement);
        }
        return contents;
    }

    /// Initializes a list of Run objects based on the given list of objects.
    /// Iterates over the provided list of objects, identifies instances of type R,
    /// and constructs Run objects while keeping track of their lengths.
    ///
    /// @param objects the list of objects to be iterated over and processed into Run instances
    ///
    /// @return a list of Run objects created from the given input list
    public static List<Run> wrap(List<Object> objects) {
        var currentLength = 0;
        var runList = new ArrayList<Run>(objects.size());
        for (int i = 0; i < objects.size(); i++) {
            var object = objects.get(i);
            if (object instanceof R run) {
                var currentRun = new Run(currentLength, i, run);
                runList.add(currentRun);
                currentLength += currentRun.length();
            }
        }
        return runList;
    }

    /// Creates a new run with the specified text and the specified run style.
    ///
    /// @param text the initial text of the run.
    ///
    /// @return the newly created run.
    public static R create(String text, RPr rPr) {
        R newStartRun = newRun(text);
        newStartRun.setRPr(rPr);
        return newStartRun;
    }

    private static void removeExpression(
            List<Object> contents,
            Run firstRun,
            int matchStartIndex,
            int matchEndIndex,
            Run lastRun,
            List<Run> affectedRuns
    ) {
        // remove the expression from the first run
        firstRun.replace(matchStartIndex, matchEndIndex, "");
        // remove all runs between first and last
        for (Run run : affectedRuns) {
            if (!Objects.equals(run, firstRun) && !Objects.equals(run, lastRun)) {
                contents.remove(run.run());
            }
        }
        // remove the expression from the last run
        lastRun.replace(matchStartIndex, matchEndIndex, "");
    }

    /// Creates a new run with the specified text and inherits the style of the parent paragraph.
    ///
    /// @param text the initial text of the run.
    ///
    /// @return the newly created run.
    public static R create(String text, PPr paragraphPr) {
        R run = newRun(text);
        applyParagraphStyle(run, paragraphPr);
        return run;
    }

    /// Applies the style of the given paragraph to the given content object (if the content object is a Run).
    ///
    /// @param run the Run to which the style should be applied.
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
    /// @param run  the run whose text to change.
    /// @param text the text to set.
    public static void setText(R run, String text) {
        run.getContent()
           .clear();
        Text textObj = newText(text);
        run.getContent()
           .add(textObj);
    }
}
