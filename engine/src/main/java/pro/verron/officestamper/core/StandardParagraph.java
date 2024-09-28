package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * <p>A "Run" defines a region of text within a docx document with a common set of properties. Word processors are
 * relatively free in splitting a paragraph of text into multiple runs, so there is no strict rule to say over how many
 * runs a word or a string of words is spread.</p>
 * <p>This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text
 * spans.
 * Create a {@link StandardParagraph} then, call methods to modify the aggregated text.
 * Finally, call {@link #asString()} to get the modified text.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.8
 */
public class StandardParagraph
        implements Paragraph {

    private final List<IndexedRun> runs = new ArrayList<>();
    private final List<Object> contents;
    private final PPr paragraphPr;
    private final P paragraph;
    private int currentPosition = 0;

    /**
     * Constructs a new ParagraphWrapper for the given paragraph.
     *
     * @param paragraph the paragraph to wrap.
     */
    public StandardParagraph(P paragraph) {
        this.paragraph = paragraph;
        this.contents = this.paragraph.getContent();
        this.paragraphPr = paragraph.getPPr();
        recalculateRuns();
    }

    /**
     * Recalculates the runs of the paragraph. This method is called automatically by the constructor, but can also be
     * called manually to recalculate the runs after a modification to the paragraph was done.
     */
    private void recalculateRuns() {
        currentPosition = 0;
        this.runs.clear();
        add(0, contents);
    }

    private int add(int index, List<Object> objects) {
        for (Object object : objects)
            index = add(index, object);
        return index;
    }

    private int add(int index, Object object) {
        if (object instanceof R r)
            return add(r, index);
        else if (object instanceof SdtRun sdtRun)
            return add(index, sdtRun);
        else if (object instanceof JAXBElement<?> jaxbElement)
            return add(index, jaxbElement.getValue());
        else
            return index + 1;
    }

    private int add(int index, SdtRun sdtRun) {
        return add(index, sdtRun.getSdtContent());
    }

    private int add(int index, SdtContent sdtContent) {
        return this.add(index, sdtContent.getContent());
    }

    /**
     * Adds a run to the aggregation.
     *
     * @param run the run to add.
     */
    private int add(R run, int index) {
        int endPosition = currentPosition + RunUtil.getLength(run);
        runs.add(new IndexedRun(currentPosition, endPosition, index, run));
        currentPosition = endPosition;
        return index + 1;
    }

    /**
     * Replaces the given expression with the replacement object within
     * the paragraph.
     * The replacement object must be a valid DOCX4J Object.
     *
     * @param placeholder the expression to be replaced.
     * @param replacement the object to replace the expression.
     */
    @Override
    public void replace(Placeholder placeholder, Object replacement) {
        if (replacement instanceof R run) {
            replaceWithRun(placeholder, run);
        }
        else if (replacement instanceof Br br) {
            replaceWithBr(placeholder, br);
        }
        else {
            throw new AssertionError("Replacement must be a R or Br, but was a " + replacement.getClass());
        }
    }

    /**
     * Returns the aggregated text over all runs.
     *
     * @return the text of all runs.
     */
    @Override
    public String asString() {
        return runs.stream()
                   .map(IndexedRun::run)
                   .map(RunUtil::getText)
                   .collect(joining());
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
        List<IndexedRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        if (singleRun) {
            IndexedRun run = affectedRuns.get(0);

            boolean expressionSpansCompleteRun = full.length() == run.length();
            boolean expressionAtStartOfRun = matchStartIndex == run.startIndex();
            boolean expressionAtEndOfRun = matchEndIndex == run.endIndex();
            boolean expressionWithinRun = matchStartIndex > run.startIndex() && matchEndIndex <= run.endIndex();

            replacement.setRPr(run.getPr());

            if (expressionSpansCompleteRun) {
                contents.remove(run.run());
                contents.add(run.indexInParent(), replacement);
                recalculateRuns();
            }
            else if (expressionAtStartOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                contents.add(run.indexInParent(), replacement);
                recalculateRuns();
            }
            else if (expressionAtEndOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                contents.add(run.indexInParent() + 1, replacement);
                recalculateRuns();
            }
            else if (expressionWithinRun) {
                int startIndex = run.indexOf(full);
                int endIndex = startIndex + full.length();
                R run1 = RunUtil.create(run.substring(0, startIndex), paragraphPr);
                R run2 = RunUtil.create(run.substring(endIndex), paragraphPr);
                contents.add(run.indexInParent(), run2);
                contents.add(run.indexInParent(), replacement);
                contents.add(run.indexInParent(), run1);
                contents.remove(run.run());
                recalculateRuns();
            }
        }
        else {
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);
            List<Object> firstRunSiblings;
            replacement.setRPr(firstRun.getPr());
            removeExpression(firstRun, matchStartIndex, matchEndIndex, lastRun, affectedRuns);
            firstRunSiblings = firstRun.isIn(paragraph)
                    ? this.contents
                    : firstRun.parent()
                              .getContent();
            // add replacement run between first and last run
            firstRunSiblings.add(firstRun.indexInParent() + 1, replacement);
            recalculateRuns();
        }
    }

    private void removeExpression(
            IndexedRun firstRun,
            int matchStartIndex,
            int matchEndIndex,
            IndexedRun lastRun,
            List<IndexedRun> affectedRuns
    ) {
        // remove the expression from the first run
        firstRun.replace(matchStartIndex, matchEndIndex, "");
        // remove all runs between first and last
        for (IndexedRun run : affectedRuns) {
            if (!Objects.equals(run, firstRun)
                && !Objects.equals(run, lastRun)) {
                contents.remove(run.run());
            }
        }
        // remove the expression from the last run
        lastRun.replace(matchStartIndex, matchEndIndex, "");
    }

    private void replaceWithBr(Placeholder placeholder, Br br) {
        for (IndexedRun indexedRun : runs) {
            var run = indexedRun.run();
            var content = run.getContent();
            var iterator = content.listIterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof JAXBElement<?> jaxbElement) {
                    element = jaxbElement.getValue();
                }
                if (element instanceof Text text) {
                    var value = text.getValue();
                    if (value.contains(placeholder.expression())) {
                        iterator.remove();
                        var iterator1 = Arrays.stream(value.split(placeholder.expression()))

                                              .iterator();
                        while (iterator1.hasNext()) {
                            var next = iterator1.next();
                            var text1 = new Text();
                            text1.setValue(next);
                            iterator.add(text1);
                            if (iterator1.hasNext())
                                iterator.add(br);
                        }
                    }
                }
            }
        }
    }

    private List<IndexedRun> getAffectedRuns(int startIndex, int endIndex) {
        return runs.stream()
                   .filter(run -> run.isTouchedByRange(startIndex, endIndex))
                   .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return asString();
    }

}
