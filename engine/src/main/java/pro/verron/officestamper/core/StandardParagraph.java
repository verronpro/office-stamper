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

    private static final Random RANDOM = new Random();
    private final DocxPart source;
    private final List<Object> contents;
    private final P p;
    private List<IndexedRun> runs;

    private StandardParagraph(DocxPart source, List<Object> paragraphContent, P p) {
        this.source = source;
        this.contents = paragraphContent;
        this.p = p;
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
    public static StandardParagraph from(DocxPart source, P paragraph) {
        return new StandardParagraph(source, paragraph.getContent(), paragraph);
    }

    /**
     * Constructs a StandardParagraph from a given CTSdtContentRun paragraph.
     *
     * @param paragraph a CTSdtContentRun object representing the content run of the paragraph
     *
     * @return a new instance of StandardParagraph based on the provided CTSdtContentRun
     */
    public static StandardParagraph from(DocxPart source, CTSdtContentRun paragraph) {
        var parent = (SdtRun) paragraph.getParent();
        var parentParent = (P) parent.getParent();
        return new StandardParagraph(source, paragraph.getContent(), parentParent);
    }

    @Override public ProcessorContext processorContext(Placeholder placeholder) {
        var comment = comment(placeholder);
        var firstRun = (R) contents.getFirst();
        return new ProcessorContext(this, firstRun, comment, placeholder);
    }

    @Override public void replace(List<P> toRemove, List<P> toAdd) {
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

    @Override public void remove() {
        WmlUtils.remove(p);
    }

    /**
     * Retrieves the P object associated with this StandardParagraph.
     *
     * @return the P object of this paragraph.
     *
     * @deprecated Not recommended, as will be replaced by other API
     */
    @Deprecated(since = "2.6", forRemoval = true) @Override public P getP() {
        return p;
    }

    /**
     * Replaces the given expression with the replacement object within
     * the paragraph.
     * The replacement object must be a valid DOCX4J Object.
     *
     * @param placeholder the expression to be replaced.
     * @param replacement the object to replace the expression.
     */
    @Override public void replace(Placeholder placeholder, Object replacement) {
        switch (replacement) {
            case R run -> replaceWithRun(placeholder, run);
            case Br br -> replaceWithBr(placeholder, br);
            default -> throw new AssertionError("Replacement must be a R or Br, but was a " + replacement.getClass());
        }
    }

    /**
     * Returns the aggregated text over all runs.
     *
     * @return the text of all runs.
     */
    @Override public String asString() {
        return runs.stream()
                   .map(IndexedRun::run)
                   .map(RunUtil::getText)
                   .collect(joining());
    }

    @Override public void apply(Consumer<P> pConsumer) {
        pConsumer.accept(p);
    }

    @Override public <T> Optional<T> parent(Class<T> aClass) {
        return parent(aClass, Integer.MAX_VALUE);
    }

    @Override
    public Collection<Comments.Comment> getComment() {
        return CommentUtil.getCommentFor(contents, source.document());
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
            IndexedRun run = affectedRuns.getFirst();

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
            IndexedRun firstRun = affectedRuns.getFirst();
            IndexedRun lastRun = affectedRuns.getLast();
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
                if (element instanceof JAXBElement<?> jaxbElement && !jaxbElement.getName().getLocalPart().equals(
                        "instrText")) element =
                        jaxbElement.getValue();
                if (element instanceof Text text) replaceWithBr(placeholder, br, text, runContentIterator);
            }
        }
    }

    private List<IndexedRun> getAffectedRuns(int startIndex, int endIndex) {
        return runs.stream()
                   .filter(run -> run.isTouchedByRange(startIndex, endIndex))
                   .toList();
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
            if (!Objects.equals(run, firstRun) && !Objects.equals(run, lastRun)) {
                contents.remove(run.run());
            }
        }
        // remove the expression from the last run
        lastRun.replace(matchStartIndex, matchEndIndex, "");
    }

    private static void replaceWithBr(
            Placeholder placeholder, Br br, Text text, ListIterator<Object> runContentIterator
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

    private Comment comment(Placeholder placeholder) {
        var id = new BigInteger(16, RANDOM);
        return StandardComment.create(source.document(), p, placeholder, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override public String toString() {
        return asString();
    }

}
