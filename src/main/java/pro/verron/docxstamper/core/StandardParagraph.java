package pro.verron.docxstamper.core;

import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.IndexedRun;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.docxstamper.api.Paragraph;
import pro.verron.docxstamper.api.Placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * <p>A "Run" defines a region of text within a docx document with a common set of properties. Word processors are
 * relatively free in splitting a paragraph of text into multiple runs, so there is no strict rule to say over how many
 * runs a word or a string of words is spread.</p>
 * <p>This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text spans.
 * Call {@link #addRun(R, int)} to add all runs that should be aggregated. Then, call
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
    private final P paragraph;
    private int currentPosition = 0;

    /**
     * Constructs a new ParagraphWrapper for the given paragraph.
     *
     * @param paragraph the paragraph to wrap.
     */
    public StandardParagraph(P paragraph) {
        this.paragraph = paragraph;
        recalculateRuns();
    }

    /**
     * Recalculates the runs of the paragraph. This method is called automatically by the constructor, but can also be
     * called manually to recalculate the runs after a modification to the paragraph was done.
     */
    private void recalculateRuns() {
        currentPosition = 0;
        this.runs.clear();
        int index = 0;
        for (Object contentElement : paragraph.getContent()) {
            if (contentElement instanceof R r && !RunUtil.getText(r)
                    .isEmpty()) {
                this.addRun(r, index);
            }
            index++;
        }
    }

    /**
     * Adds a run to the aggregation.
     *
     * @param run the run to add.
     */
    private void addRun(R run, int index) {
        int startIndex = currentPosition;
        int endIndex = currentPosition + RunUtil.getText(run)
                .length() - 1;
        runs.add(new IndexedRun(startIndex, endIndex, index, run));
        currentPosition = endIndex + 1;
    }

    /**
     * Replaces the given expression with the replacement object within
     * the paragraph.
     * The replacement object must be a valid DOCX4J Object.
     *
     * @param placeholder  the expression to be replaced.
     * @param replacement the object to replace the expression.
     */
    @Override
    public void replace(Placeholder placeholder, R replacement) {
        String text = asString();
        String full = placeholder.expression();
        int matchStartIndex = text.indexOf(full);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + full.length() - 1;
        List<IndexedRun> affectedRuns = getAffectedRuns(matchStartIndex,
                                                        matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        if (singleRun) {
            IndexedRun run = affectedRuns.get(0);


            boolean expressionSpansCompleteRun =
                    full.length() == RunUtil.getText(run.run())
                            .length();
            boolean expressionAtStartOfRun = matchStartIndex == run.startIndex();
            boolean expressionAtEndOfRun = matchEndIndex == run.endIndex();
            boolean expressionWithinRun = matchStartIndex > run.startIndex() && matchEndIndex < run.endIndex();

            replacement.setRPr(run.run()
                                       .getRPr());

            if (expressionSpansCompleteRun) {
                this.paragraph.getContent()
                        .remove(run.run());
                this.paragraph.getContent()
                        .add(run.indexInParent(), replacement);
                recalculateRuns();
            } else if (expressionAtStartOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                this.paragraph.getContent()
                        .add(run.indexInParent(), replacement);
                recalculateRuns();
            } else if (expressionAtEndOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                this.paragraph.getContent()
                        .add(run.indexInParent() + 1, replacement);
                recalculateRuns();
            } else if (expressionWithinRun) {
                String runText = RunUtil.getText(run.run());
                int startIndex = runText.indexOf(full);
                int endIndex = startIndex + full.length();
                R run1 = RunUtil.create(runText.substring(0, startIndex),
                                        this.paragraph);
                R run2 = RunUtil.create(runText.substring(endIndex),
                                        this.paragraph);
                this.paragraph.getContent()
                        .add(run.indexInParent(), run2);
                this.paragraph.getContent()
                        .add(run.indexInParent(), replacement);
                this.paragraph.getContent()
                        .add(run.indexInParent(), run1);
                this.paragraph.getContent()
                        .remove(run.run());
                recalculateRuns();
            }
        } else {
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);
            replacement.setRPr(firstRun.run()
                                       .getRPr());
            // remove the expression from first and last run
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            lastRun.replace(matchStartIndex, matchEndIndex, "");

            // remove all runs between first and last
            for (IndexedRun run : affectedRuns) {
                if (!Objects.equals(run, firstRun) && !Objects.equals(run,
                                                                      lastRun)) {
                    this.paragraph.getContent()
                            .remove(run.run());
                }
            }

            // add replacement run between first and last run
            this.paragraph.getContent()
                    .add(firstRun.indexInParent() + 1, replacement);

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
     * Returns the list of runs that are aggregated. Depending on what modifications were done to the aggregated text,
     * this list may not return the same runs initially added to the aggregator.
     *
     * @return the list of aggregated runs.
     */
    private List<R> getRuns() {
        return runs.stream()
                .map(IndexedRun::run)
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
