package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * <p>A "Run" defines a region of text within a docx document with a common set of properties. Word processors are
 * relatively free in splitting a paragraph of text into multiple runs, so there is no strict rule to say over how many
 * runs a word or a string of words is spread.</p>
 * <p>This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text
 * spans.
 * Call {@link #add(R, int)} to add all runs that should be aggregated. Then, call
 * methods to modify the aggregated text. Finally, call {@link #asString()} to get the modified text.
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
        if (!(replacement instanceof R replacementRun))
            throw new AssertionError("replacement must be a R");

        String text = asString();
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

            replacementRun.setRPr(run.getPr());

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
        else if (affectedRuns.get(0)
                             .run()
                             .getParent() == paragraph) {
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);
            replacementRun.setRPr(firstRun.getPr());
            // remove the expression from first and last run
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            lastRun.replace(matchStartIndex, matchEndIndex, "");

            // remove all runs between first and last
            for (IndexedRun run : affectedRuns) {
                if (!Objects.equals(run, firstRun)
                    && !Objects.equals(run, lastRun)) {
                    contents.remove(run.run());
                }
            }

            // add replacement run between first and last run
            contents.add(firstRun.indexInParent() + 1, replacement);

            recalculateRuns();
        } else {
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);

            var siblings = ((ContentAccessor) firstRun.run()
                                                      .getParent()).getContent();
            replacementRun.setRPr(firstRun.getPr());
            // remove the expression from first and last run
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            lastRun.replace(matchStartIndex, matchEndIndex, "");

            // remove all runs between first and last
            for (IndexedRun run : affectedRuns) {
                if (!Objects.equals(run, firstRun)
                    && !Objects.equals(run, lastRun)) {
                    siblings.remove(run.run());
                }
            }

            // add replacement run between first and last run
            siblings.add(siblings.indexOf(firstRun) + 1, replacement);

            recalculateRuns();
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
