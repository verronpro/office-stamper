package pro.verron.docxstamper.core;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.wml.R;
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
 * <p>This class aggregates multiple runs so they can be treated as a single text, no matter how many runs the text
 * spans.
 * Call {@link #addRun(R, int)} to add all runs that should be aggregated. Then, call
 * methods to modify the aggregated text. Finally, call {@link #asString()} to get the modified text.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.8
 */
public class PowerpointParagraph
        implements Paragraph<CTRegularTextRun> {
    private final List<PowerpointRun> runs = new ArrayList<>();
    private final CTTextParagraph paragraph;
    private int currentPosition = 0;

    /**
     * Constructs a new ParagraphWrapper for the given paragraph.
     *
     * @param paragraph the paragraph to wrap.
     */
    public PowerpointParagraph(CTTextParagraph paragraph) {
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

    /**
     * Replaces the given expression with the replacement object within
     * the paragraph.
     * The replacement object must be a valid DOCX4J Object.
     *
     * @param placeholder the expression to be replaced.
     * @param replacement the object to replace the expression.
     */
    @Override
    public void replace(Placeholder placeholder, CTRegularTextRun replacement) {
        String text = asString();
        String full = placeholder.expression();
        int matchStartIndex = text.indexOf(full);
        if (matchStartIndex == -1) {
            // nothing to replace
            return;
        }
        int matchEndIndex = matchStartIndex + full.length() - 1;
        List<PowerpointRun> affectedRuns = getAffectedRuns(matchStartIndex,
                matchEndIndex);

        boolean singleRun = affectedRuns.size() == 1;

        List<Object> runs = this.paragraph.getEGTextRun();
        if (singleRun) {
            PowerpointRun run = affectedRuns.get(0);


            boolean expressionSpansCompleteRun =
                    full.length() == run.run()
                                        .getT()
                                        .length();
            boolean expressionAtStartOfRun = matchStartIndex == run.startIndex();
            boolean expressionAtEndOfRun = matchEndIndex == run.endIndex();
            boolean expressionWithinRun = matchStartIndex > run.startIndex() && matchEndIndex < run.endIndex();

            replacement.setRPr(run.run()
                                  .getRPr());

            if (expressionSpansCompleteRun) {
                runs.remove(run.run());
                runs.add(run.indexInParent(), replacement);
                recalculateRuns();
            }
            else if (expressionAtStartOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                runs.add(run.indexInParent(), replacement);
                recalculateRuns();
            }
            else if (expressionAtEndOfRun) {
                run.replace(matchStartIndex, matchEndIndex, "");
                runs.add(run.indexInParent() + 1, replacement);
                recalculateRuns();
            }
            else if (expressionWithinRun) {
                String runText = run.run()
                                    .getT();
                int startIndex = runText.indexOf(full);
                int endIndex = startIndex + full.length();
                String substring1 = runText.substring(0, startIndex);
                CTRegularTextRun run1 = create(substring1, this.paragraph);
                String substring2 = runText.substring(endIndex);
                CTRegularTextRun run2 = create(substring2, this.paragraph);
                runs.add(run.indexInParent(), run2);
                runs.add(run.indexInParent(), replacement);
                runs.add(run.indexInParent(), run1);
                runs.remove(run.run());
                recalculateRuns();
            }
        }
        else {
            PowerpointRun firstRun = affectedRuns.get(0);
            PowerpointRun lastRun = affectedRuns.get(affectedRuns.size() - 1);
            replacement.setRPr(firstRun.run()
                                       .getRPr());
            // remove the expression from first and last run
            firstRun.replace(matchStartIndex, matchEndIndex, "");
            lastRun.replace(matchStartIndex, matchEndIndex, "");

            // remove all runs between first and last
            for (PowerpointRun run : affectedRuns) {
                if (!Objects.equals(run, firstRun) && !Objects.equals(run,
                        lastRun)) {
                    runs
                            .remove(run.run());
                }
            }

            // add replacement run between first and last run
            runs
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
        /*StringBuilder builder = new StringBuilder();
        List<Object> egTextRun = paragraph.getEGTextRun();
        for (Object object : egTextRun) {
            if (object instanceof CTRegularTextRun run)
                builder.append(run.getT());
            else if (object instanceof CTTextField text)
                builder.append(text.getT());
            else if (object instanceof CTTextLineBreak)
                builder.append("\n");
        }
        return builder.toString();*/
        return runs.stream()
                   .map(PowerpointRun::run)
                   .map(CTRegularTextRun::getT)
                   .collect(joining()) + "\n";
    }

    private List<PowerpointRun> getAffectedRuns(int startIndex, int endIndex) {
        return runs.stream()
                   .filter(run -> run.isTouchedByRange(startIndex, endIndex))
                   .toList();
    }

    private static CTRegularTextRun create(
            String text,
            CTTextParagraph parentParagraph
    ) {
        CTRegularTextRun run = new CTRegularTextRun();
        run.setT(text);
        applyParagraphStyle(parentParagraph, run);
        return run;
    }

    private static void applyParagraphStyle(
            CTTextParagraph p,
            CTRegularTextRun run
    ) {
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

    private static CTTextCharacterProperties apply(
            CTTextCharacterProperties source,
            CTTextCharacterProperties destination
    ) {
        if (source.getAltLang() != null)
            destination.setAltLang(source.getAltLang());
        if (source.getBaseline() != null)
            destination.setBaseline(source.getBaseline());
        if (source.getBmk() != null)
            destination.setBmk(source.getBmk());
        if (source.getBlipFill() != null)
            destination.setBlipFill(source.getBlipFill());
        if (source.getCap() != null)
            destination.setCap(source.getCap());
        if (source.getCs() != null)
            destination.setCs(source.getCs());
        if (source.getGradFill() != null)
            destination.setGradFill(source.getGradFill());
        if (source.getGrpFill() != null)
            destination.setGrpFill(source.getGrpFill());
        if (source.getHighlight() != null)
            destination.setHighlight(source.getHighlight());
        if (source.getHlinkClick() != null)
            destination.setHlinkClick(source.getHlinkClick());
        if (source.getHlinkMouseOver() != null)
            destination.setHlinkMouseOver(source.getHlinkMouseOver());
        if (source.getKern() != null)
            destination.setKern(source.getKern());
        if (source.getLang() != null)
            destination.setLang(source.getLang());
        if (source.getLn() != null)
            destination.setLn(source.getLn());
        if (source.getLatin() != null)
            destination.setLatin(source.getLatin());
        if (source.getNoFill() != null)
            destination.setNoFill(source.getNoFill());
        if (source.getPattFill() != null)
            destination.setPattFill(source.getPattFill());
        if (source.getSpc() != null)
            destination.setSpc(source.getSpc());
        if (source.getSym() != null)
            destination.setSym(source.getSym());
        if (source.getStrike() != null)
            destination.setStrike(source.getStrike());
        if (source.getSz() != null)
            destination.setSz(source.getSz());
        if (source.getSmtId() != 0)
            destination.setSmtId(source.getSmtId());
        if (source.getU() != null)
            destination.setU(source.getU());
        if (source.getUFill() != null)
            destination.setUFill(source.getUFill());
        if (source.getUFillTx() != null)
            destination.setUFillTx(source.getUFillTx());
        if (source.getULn() != null)
            destination.setULn(source.getULn());
        if (source.getULnTx() != null)
            destination.setULnTx(source.getULnTx());
        if (source.getULnTx() != null)
            destination.setULnTx(source.getULnTx());
        return destination;
    }

    /**
     * Returns the list of runs that are aggregated. Depending on what modifications were done to the aggregated text,
     * this list may not return the same runs initially added to the aggregator.
     *
     * @return the list of aggregated runs.
     */
    private List<CTRegularTextRun> getRuns() {
        return runs.stream()
                   .map(PowerpointRun::run)
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
