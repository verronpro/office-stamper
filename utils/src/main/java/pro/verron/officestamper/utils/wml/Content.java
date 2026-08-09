package pro.verron.officestamper.utils.wml;

import org.docx4j.XmlUtils;
import org.docx4j.wml.*;
import org.jvnet.jaxb.lang.Child;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static org.docx4j.XmlUtils.unwrap;
import static pro.verron.officestamper.utils.wml.WmlFactory.*;
import static pro.verron.officestamper.utils.wml.WmlUtils.create;

public class Content {
    private final ContentAccessor parent;
    private final List<Object> elements;
    private final List<Element> elementList;

    public Content(ContentAccessor parent, List<Object> elements) {
        this.parent = parent;
        this.elements = elements;
        elementList = elements.stream().map(Element::new).toList();
    }

    public Content(ContentAccessor parent) {
        this(parent, parent.getContent());
    }

    /// Deletes all elements associated with the specified comment from the
    /// provided list of items.
    ///
    /// @param commentId the ID of the comment to be deleted
    /// @param items     the list of items from which elements associated with
    /// the comment will be deleted
    public void deleteCommentFromElements(BigInteger commentId) {

        record DeletableItems(List<Object> container, List<Object> items) {
            private static List<DeletableItems> from(List<Object> items, Object item) {
                return Collections.singletonList(new DeletableItems(items, List.of(item)));
            }

            static List<DeletableItems> findAll(List<Object> items, BigInteger commentId) {
                Predicate<BigInteger> predicate = bi -> Objects.equals(bi, commentId);
                List<DeletableItems> elementsToRemove = new ArrayList<>();
                items.forEach(item -> {
                    Object unwrapped = unwrap(item);
                    // Recursively finds deletable items associated with
                    // comment ID
                    elementsToRemove.addAll(switch (unwrapped) {
                        case CTSmartTagRun str when str.getContent()
                                .stream()
                                .anyMatch(i -> i instanceof CommentRangeStart crs && predicate.test(crs.getId())) ->
                                from(items, item);
                        case CommentRangeStart crs when predicate.test(crs.getId()) -> from(items, item);
                        case CommentRangeEnd cre when predicate.test(cre.getId()) -> from(items, item);
                        case R.CommentReference rcr when predicate.test(rcr.getId()) -> from(items, item);
                        case ContentAccessor ca -> (Collection<DeletableItems>) findAll(ca.getContent(), commentId);
                        case SdtRun sdtRun -> {
                            var ca = sdtRun.getSdtContent();
                            yield (Collection<DeletableItems>) findAll(ca.getContent(), commentId);
                        }
                        default -> emptyList();
                    });
                });
                return elementsToRemove;
            }
        }

        DeletableItems.findAll(elements, commentId).forEach(p -> p.container.removeAll(p.items));
    }

    /// Inserts a smart tag with the specified element type into the given
    /// paragraph at the position of the expression.
    ///
    /// @param element    the element type for the smart tag
    /// @param expression the expression to replace with the smart tag
    /// @param start      the start index of the expression
    /// @param end        the end index of the expression
    public void insertSmartTag(String element, String expression, int start, int end) {
        var run = newRun(expression);
        var smartTag = newSmartTag("officestamper", List.of(newCtAttr("type", element)), run);
        findFirstAffectedRunPr(start, end).ifPresent(run::setRPr);
        replace(List.of(smartTag), start, end);
    }

    /// Finds the first affected run properties within the specified range.
    ///
    /// @param parent the [Parent] to search in
    /// @param start  the start index of the range
    /// @param end    the end index of the range
    /// @return an [Optional] containing the [RPr] if found, or an empty
    /// [Optional] if not found
    public Optional<RPr> findFirstAffectedRunPr(int start, int end) {
        return sub(start, end).firstR().map(R::getRPr);
    }

    /// Replaces all occurrences of the specified expression with the provided run objects.
    ///
    /// @param parent     the [Parent] in which to replace the expression
    /// @param expression the expression to replace
    /// @param insert     the list of objects to insert
    /// @param onRPr      a consumer to handle [RPr] properties
    public void replaceExpressionWithRun(String expression, List<Object> insert, Consumer<RPr> onRPr) {
        var text = WmlUtils.asString(this);
        int matchStartIndex = text.indexOf(expression);
        if (matchStartIndex == -1) /*nothing to replace*/ return;
        int matchEndIndex = matchStartIndex + expression.length();
        this.findFirstAffectedRunPr(matchStartIndex, matchEndIndex).ifPresent(onRPr);
        replace(insert, matchStartIndex, matchEndIndex);
    }

    /// Replaces content within the specified range with the provided insert
    /// objects.
    ///
    /// @param parent     the [Parent] in which to replace content
    /// @param insert     the list of objects to insert
    /// @param startIndex the start index of the range to replace
    /// @param endIndex   the end index of the range to replace
    public void replace(List<Object> insert, int startIndex, int endIndex) {
        var affectedRuns = WmlUtils.StandardRun.wrap(new DocxIterator(this).selectClass(R.class))
                .stream()
                .filter(sr -> sr.isTouchedByRange(startIndex, endIndex))
                .toList();
        var firstRun = affectedRuns.getFirst();
        var firstR = affectedRuns.getFirst().run();
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
            } else if (expressionAtStartOfRun) {
                firstRun.replace(startIndex, endIndex, "");
                firstSiblings.addAll(firstIndex, insert);
            } else if (expressionAtEndOfRun) {
                firstRun.replace(startIndex, endIndex, "");
                firstSiblings.addAll(firstIndex + 1, insert);
            } else if (expressionWithinRun) {
                var originalRun = firstRun.run();
                var originalRPr = originalRun.getRPr();
                var newStartRun = create(firstRun.left(startIndex), originalRPr);
                var newEndRun = create(firstRun.right(endIndex), originalRPr);
                firstSiblings.remove(firstIndex);
                firstSiblings.addAll(firstIndex, WmlUtils.wrap(newStartRun, insert, newEndRun));
            }
        } else {
            WmlUtils.StandardRun lastRun = affectedRuns.getLast();
            WmlUtils.removeExpression(firstSiblings, firstRun, startIndex, endIndex, lastRun, affectedRuns);
            // add replacement run between first and last run
            firstSiblings.addAll(firstIndex + 1, insert);
        }
    }

    public List<Element> getContent() {
        return elementList;
    }

    public void remove() {
        parent.getContent().remove(elements);
    }

    public Object get() {
        return parent;
    }

    public Element getFirst() {
        return elementList.getFirst();
    }

    public void clear() {
        parent.getContent().removeAll(elements);
    }

    public Content copy() {
        return new Content(XmlUtils.deepCopy(parent));
    }

    public Element addEmptyParagraph() {
        var p = WmlFactory.newParagraph();
        elements.addLast(p);
        return new Element(p);
    }

    public void add(int index, List<Element> content) {
        elements.forEach(element -> {
            if (element instanceof Child child) child.setParent(parent);
        });
        elements.addAll(index, content.stream().map(Element::get).toList());
    }

    public int size() {
        return elements.size();
    }

    public void apply(Consumer<ContentAccessor> pConsumer) {
        pConsumer.accept(() -> elements);
    }

    public Content sub(int start, int end) {
        var iterator = elements.stream().filter(R.class::isInstance).map(R.class::cast).iterator();
        var runs = WmlUtils.StandardRun.wrap(iterator);

        List<Object> affectedRuns = new ArrayList<>();
        for (WmlUtils.StandardRun run : runs) {
            if (run.isTouchedByRange(start, end)) {
                R r = run.run();
                affectedRuns.add(r);
            }
        }
        return new Content(parent, affectedRuns);
    }

    public Optional<R> firstR() {
        return elements.stream().filter(R.class::isInstance).map(R.class::cast).findFirst();
    }

    public static class Element {
        private final Object element;

        public Element(Object element) {
            this.element = element;
        }

        public Object get() {
            return element;
        }

        public boolean isParagraph() {
            return element instanceof P;
        }
    }
}
