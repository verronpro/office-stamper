package pro.verron.officestamper.core;

import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import static java.util.Arrays.stream;
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

    private List<IndexedRun> runs;
    private final List<Object> contents;

    private StandardParagraph(List<Object> paragraphContent) {
        this.contents = paragraphContent;
        this.runs = initializeRunList(contents);
    }


    /**
     * Calculates the runs of the paragraph.
     * This method is called automatically by the constructor, but can also be
     * called manually to recalculate the runs after a modification to the paragraph was done.
     */
    private static List<IndexedRun> initializeRunList(List<Object> objects) {
        int currentLength = 0;
        var runList = new ArrayList<IndexedRun>(objects.size());
        for (int i = 0; i < objects.size(); i++) {
            Object object = objects.get(i);
            if (object instanceof R run) {
                int nextLength = currentLength + RunUtil.getLength(run);
                runList.add(new IndexedRun(currentLength, nextLength, i, run));
                currentLength = nextLength;
            }
        }
        return runList;
    }

    /**
     * Constructs a new ParagraphWrapper for the given paragraph.
     */
    public static StandardParagraph from(P paragraph) {
        return new StandardParagraph(paragraph.getContent());
    }

    public static StandardParagraph from(CTSdtContentRun paragraph) {
        return new StandardParagraph(paragraph.getContent());
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
            IndexedRun firstRun = affectedRuns.get(0);
            IndexedRun lastRun = affectedRuns.get(affectedRuns.size() - 1);
            replacement.setRPr(firstRun.getPr());
            removeExpression(firstRun, matchStartIndex, matchEndIndex, lastRun, affectedRuns);
            // add replacement run between first and last run
            contents.add(firstRun.indexInParent() + 1, replacement);
        }
        this.runs = initializeRunList(contents);
    }

    private void replaceWithBr(Placeholder placeholder, Br br) {
        for (IndexedRun indexedRun : runs) {
            var runContentIterator = indexedRun.run()
                                               .getContent()
                                               .listIterator();
            while (runContentIterator.hasNext()) {
                Object element = runContentIterator.next();
                if (element instanceof JAXBElement<?> jaxbElement)
                    element = jaxbElement.getValue();
                if (element instanceof Text text)
                    replaceWithBr(placeholder, br, text, runContentIterator);
            }
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
            var subText = createText(runLinebreakIterator.next());
            runContentIterator.add(subText);
            if (runLinebreakIterator.hasNext()) runContentIterator.add(br);
        }
    }

    private static Text createText(String next) {
        var replacement = new Text();
        replacement.setValue(next);
        return replacement;
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
