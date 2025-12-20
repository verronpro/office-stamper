package pro.verron.officestamper.experimental;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.wml.Comments;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.Table;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// A "Run" defines a region of text within a docx document with a common set of properties. Word processors are
/// relatively free in splitting a paragraph of text into multiple runs, so there is no strict rule to say over how many
/// runs a word or a string of words is spread.
///
/// This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text
/// spans.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.8
public class PowerpointParagraph
        implements Paragraph {

    private final PptxPart part;
    private final List<PowerpointRun> runs = new ArrayList<>();
    private final CTTextParagraph paragraph;
    private int currentPosition = 0;

    /// Constructs a new ParagraphWrapper for the given paragraph.
    ///
    /// @param part the source of the paragraph.
    /// @param paragraph the paragraph to wrap.
    public PowerpointParagraph(PptxPart part, CTTextParagraph paragraph) {
        this.part = part;
        this.paragraph = paragraph;
        recalculateRuns();
    }

    /// Recalculates the runs of the paragraph. This method is called automatically by the constructor, but can also be
    /// called manually to recalculate the runs after a modification to the paragraph was done.
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

    /// Adds a run to the aggregation.
    ///
    /// @param run the run to add.
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

    private static CTTextCharacterProperties apply(
            CTTextCharacterProperties source
    ) {
        return apply(source, new CTTextCharacterProperties());
    }

    @Override
    public void replace(List<P> toRemove, List<P> toAdd) {
        int index = siblings().indexOf(paragraph);
        if (index < 0) throw new OfficeStamperException("Impossible");

        siblings().addAll(index, toAdd);
        siblings().removeAll(toRemove);
    }

    @Override
    public void remove() {
        WmlUtils.remove(paragraph);
    }

    /// Replaces a placeholder within the paragraph with the content from the given insert, preserving formatting.
    ///
    /// @param insert the content to replace the placeholder with; must be a valid and compatible text run
    @Override
    public void replace(String expression, Insert insert) {
        var elements = insert.elements();
        if (elements.size() != 1) throw new AssertionError("Insert must contain exactly one element");
        var element = elements.getFirst();
        if (!(element instanceof CTRegularTextRun replacementRun))
            throw new AssertionError("Insert '%s' is not a unique element of expected type '%s'".formatted(element,
                    CTRegularTextRun.class));

        String text = asString();
        int matchStartIndex = text.indexOf(expression);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + expression.length() - 1;
        List<PowerpointRun> affectedRuns = getAffectedRuns(matchStartIndex, matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        List<Object> textRun = this.paragraph.getEGTextRun();
        replacementRun.setRPr(affectedRuns.getFirst()
                                          .run()
                                          .getRPr());
        if (singleRun) singleRun(replacementRun,
                expression,
                matchStartIndex,
                matchEndIndex,
                textRun,
                affectedRuns.getFirst(),
                affectedRuns.getLast());
        else multipleRuns(replacementRun,
                affectedRuns,
                matchStartIndex,
                matchEndIndex,
                textRun,
                affectedRuns.getFirst(),
                affectedRuns.getLast());

    }

    @Override
    public void replace(Object from, Object to, Insert insert) {
        throw new OfficeStamperException("Not yet implemented");
    }

    /// Returns the aggregated text over all runs.
    ///
    /// @return the text of all runs.
    @Override
    public String asString() {
        return runs.stream()
                   .map(PowerpointRun::run)
                   .map(CTRegularTextRun::getT)
                   .collect(joining()) + "\n";
    }

    @Override
    public void apply(Consumer<ContentAccessor> pConsumer) {
        pConsumer.accept(paragraph::getEGTextRun);
    }

    @Override
    public <T> Optional<T> parent(Class<T> aClass) {
        return parent(aClass, Integer.MAX_VALUE);
    }

    @Override
    public Collection<Comments.Comment> getComment() {
        return CommentUtil.getCommentFor(paragraph::getEGTextRun, part.document());
    }

    @Override
    public Optional<Table.Row> parentTableRow() {
        return Optional.empty();
    }

    @Override
    public Optional<Table> parentTable() {
        return Optional.empty();
    }

    private List<Object> siblings() {
        return this.parent(ContentAccessor.class, 1)
                   .orElseThrow(throwing("Not a standard Child with common parent"))
                   .getContent();
    }

    private <T> Optional<T> parent(Class<T> aClass, int depth) {
        return WmlUtils.getFirstParentWithClass(paragraph, aClass, depth);
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

    private List<PowerpointRun> getAffectedRuns(int startIndex, int endIndex) {
        return runs.stream()
                   .filter(run -> run.isTouchedByRange(startIndex, endIndex))
                   .toList();
    }

    /// {@inheritDoc}
    @Override
    public String toString() {
        return asString();
    }
}
