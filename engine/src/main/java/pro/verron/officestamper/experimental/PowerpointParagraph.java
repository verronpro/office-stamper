package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.StandardComment;
import pro.verron.officestamper.utils.WmlFactory;
import pro.verron.officestamper.utils.WmlUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/**
 * <p>A "Run" defines a region of text within a docx document with a common set of properties. Word processors are
 * relatively free in splitting a paragraph of text into multiple runs, so there is no strict rule to say over how many
 * runs a word or a string of words is spread.</p>
 * <p>This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text
 * spans.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.8
 */
public class PowerpointParagraph
        implements Paragraph {

    private static final Random RANDOM = new Random();
    private final DocxPart source;
    private final List<PowerpointRun> runs = new ArrayList<>();
    private final CTTextParagraph paragraph;
    private int currentPosition = 0;

    /**
     * Constructs a new ParagraphWrapper for the given paragraph.
     *
     * @param paragraph the paragraph to wrap.
     */
    public PowerpointParagraph(PptxPart source, CTTextParagraph paragraph) {
        this.source = source;
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
        for (Object contentElement : paragraph.getEGTextRun()) {
            if (contentElement instanceof CTRegularTextRun r && !r.getT()
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
    private void addRun(CTRegularTextRun run, int index) {
        int startIndex = currentPosition;
        int endIndex = currentPosition + run.getT()
                                            .length() - 1;
        runs.add(new PowerpointRun(startIndex, endIndex, index, run));
        currentPosition = endIndex + 1;
    }

    private static CTTextCharacterProperties apply(
            CTTextCharacterProperties source,
            CTTextCharacterProperties destination
    ) {
        ofNullable(source.getAltLang()).ifPresent(destination::setAltLang);
        ofNullable(source.getBaseline()).ifPresent(destination::setBaseline);
        ofNullable(source.getBmk()).ifPresent(destination::setBmk);
        ofNullable(source.getBlipFill()).ifPresent(destination::setBlipFill);
        ofNullable(source.getCap()).ifPresent(destination::setCap);
        ofNullable(source.getCs()).ifPresent(destination::setCs);
        ofNullable(source.getGradFill()).ifPresent(destination::setGradFill);
        ofNullable(source.getGrpFill()).ifPresent(destination::setGrpFill);
        ofNullable(source.getHighlight()).ifPresent(destination::setHighlight);
        ofNullable(source.getHlinkClick()).ifPresent(destination::setHlinkClick);
        ofNullable(source.getHlinkMouseOver()).ifPresent(destination::setHlinkMouseOver);
        ofNullable(source.getKern()).ifPresent(destination::setKern);
        ofNullable(source.getLang()).ifPresent(destination::setLang);
        ofNullable(source.getLn()).ifPresent(destination::setLn);
        ofNullable(source.getLatin()).ifPresent(destination::setLatin);
        ofNullable(source.getNoFill()).ifPresent(destination::setNoFill);
        ofNullable(source.getPattFill()).ifPresent(destination::setPattFill);
        ofNullable(source.getSpc()).ifPresent(destination::setSpc);
        ofNullable(source.getSym()).ifPresent(destination::setSym);
        ofNullable(source.getStrike()).ifPresent(destination::setStrike);
        ofNullable(source.getSz()).ifPresent(destination::setSz);
        destination.setSmtId(source.getSmtId());
        ofNullable(source.getU()).ifPresent(destination::setU);
        ofNullable(source.getUFill()).ifPresent(destination::setUFill);
        ofNullable(source.getUFillTx()).ifPresent(destination::setUFillTx);
        ofNullable(source.getULn()).ifPresent(destination::setULn);
        ofNullable(source.getULnTx()).ifPresent(destination::setULnTx);
        ofNullable(source.getULnTx()).ifPresent(destination::setULnTx);
        return destination;
    }

    @Override
    public ProcessorContext processorContext(Placeholder placeholder) {
        var comment = comment(placeholder);
        var firstRun = (R) paragraph.getEGTextRun()
                                    .getFirst();
        return new ProcessorContext(this, firstRun, comment, placeholder);
    }

    @Override
    public void remove() {
        WmlUtils.remove(getP());
    }

    @Override
    public void replace(List<P> toRemove, List<P> toAdd) {
        int index = siblings().indexOf(getP());
        if (index < 0) throw new OfficeStamperException("Impossible");

        siblings().addAll(index, toAdd);
        siblings().removeAll(toRemove);
    }

    @Override
    public P getP() {
        var p = WmlFactory.newParagraph(paragraph.getEGTextRun());
        p.setParent(paragraph.getParent());
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
    @Override
    public void replace(Placeholder placeholder, Object replacement) {
        if (!(replacement instanceof CTRegularTextRun replacementRun))
            throw new AssertionError("replacement is not a CTRegularTextRun");
        String text = asString();
        String full = placeholder.expression();
        int matchStartIndex = text.indexOf(full);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + full.length() - 1;
        List<PowerpointRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        List<Object> textRun = this.paragraph.getEGTextRun();
        replacementRun.setRPr(affectedRuns.getFirst()
                                          .run()
                                          .getRPr());
        if (singleRun) singleRun(replacement,
                full,
                matchStartIndex,
                matchEndIndex,
                textRun,
                affectedRuns.getFirst(),
                affectedRuns.getLast());
        else multipleRuns(replacement,
                affectedRuns,
                matchStartIndex,
                matchEndIndex,
                textRun,
                affectedRuns.getFirst(),
                affectedRuns.getLast());

    }

    private void singleRun(
            Object replacement,
            String full,
            int matchStartIndex,
            int matchEndIndex,
            List<Object> runs,
            PowerpointRun firstRun,
            PowerpointRun lastRun
    ) {
        assert firstRun == lastRun;
        boolean expressionSpansCompleteRun = full.length() == firstRun.run()
                                                                      .getT()
                                                                      .length();
        boolean expressionAtStartOfRun = matchStartIndex == firstRun.startIndex();
        boolean expressionAtEndOfRun = matchEndIndex == firstRun.endIndex();
        boolean expressionWithinRun = matchStartIndex > firstRun.startIndex() && matchEndIndex < firstRun.endIndex();


        if (expressionSpansCompleteRun) {
            runs.remove(firstRun.run());
            runs.add(firstRun.indexInParent(), replacement);
            recalculateRuns();
        }
        else if (expressionAtStartOfRun) {
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            runs.add(firstRun.indexInParent(), replacement);
            recalculateRuns();
        }
        else if (expressionAtEndOfRun) {
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            runs.add(firstRun.indexInParent() + 1, replacement);
            recalculateRuns();
        }
        else if (expressionWithinRun) {
            String runText = firstRun.run()
                                     .getT();
            int startIndex = runText.indexOf(full);
            int endIndex = startIndex + full.length();
            String substring1 = runText.substring(0, startIndex);
            CTRegularTextRun run1 = create(substring1, this.paragraph);
            String substring2 = runText.substring(endIndex);
            CTRegularTextRun run2 = create(substring2, this.paragraph);
            runs.add(firstRun.indexInParent(), run2);
            runs.add(firstRun.indexInParent(), replacement);
            runs.add(firstRun.indexInParent(), run1);
            runs.remove(firstRun.run());
            recalculateRuns();
        }
    }

    private void multipleRuns(
            Object replacement,
            List<PowerpointRun> affectedRuns,
            int matchStartIndex,
            int matchEndIndex,
            List<Object> runs,
            PowerpointRun firstRun,
            PowerpointRun lastRun
    ) {
        // remove the expression from first and last run
        firstRun.replace(matchStartIndex, matchEndIndex, "");
        lastRun.replace(matchStartIndex, matchEndIndex, "");

        // remove all runs between first and last
        for (PowerpointRun run : affectedRuns) {
            if (!Objects.equals(run, firstRun) && !Objects.equals(run, lastRun)) {
                runs.remove(run.run());
            }
        }

        // add replacement run between first and last run
        runs.add(firstRun.indexInParent() + 1, replacement);

        recalculateRuns();
    }

    private static CTRegularTextRun create(String text, CTTextParagraph parentParagraph) {
        CTRegularTextRun run = new CTRegularTextRun();
        run.setT(text);
        applyParagraphStyle(parentParagraph, run);
        return run;
    }

    private static void applyParagraphStyle(CTTextParagraph p, CTRegularTextRun run) {
        var properties = p.getPPr();
        if (properties == null) return;

        var textCharacterProperties = properties.getDefRPr();
        if (textCharacterProperties == null) return;

        run.setRPr(apply(textCharacterProperties));
    }

    /**
     * Returns the aggregated text over all runs.
     *
     * @return the text of all runs.
     */
    @Override
    public String asString() {
        return runs.stream()
                   .map(PowerpointRun::run)
                   .map(CTRegularTextRun::getT)
                   .collect(joining()) + "\n";
    }

    @Override
    public void apply(Consumer<P> pConsumer) {
        pConsumer.accept(getP());
    }

    @Override
    public Collection<Comments.Comment> getComment() {
        return CommentUtil.getCommentFor(paragraph.getEGTextRun(), source.document());
    }

    private List<PowerpointRun> getAffectedRuns(int startIndex, int endIndex) {
        return runs.stream()
                   .filter(run -> run.isTouchedByRange(startIndex, endIndex))
                   .toList();
    }

    @Override
    public <T> Optional<T> parent(Class<T> aClass) {
        return parent(aClass, Integer.MAX_VALUE);
    }

    private List<Object> siblings() {
        return this.parent(ContentAccessor.class, 1)
                   .orElseThrow(throwing("Not a standard Child with common parent"))
                   .getContent();
    }

    private static CTTextCharacterProperties apply(
            CTTextCharacterProperties source
    ) {
        return apply(source, new CTTextCharacterProperties());
    }

    private <T> Optional<T> parent(Class<T> aClass, int depth) {
        return WmlUtils.getFirstParentWithClass(getP(), aClass, depth);
    }

    private Comment comment(Placeholder placeholder) {
        var parent = getP();
        var id = new BigInteger(16, RANDOM);
        return StandardComment.create(source.document(), parent, placeholder, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return asString();
    }
}
