package org.wickedsource.docxstamper.processor.displayif;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.wickedsource.docxstamper.util.ObjectDeleter;
import pro.verron.officestamper.api.AbstractCommentProcessor;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.core.PlaceholderReplacer;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.docx4j.TextUtils.getText;

/**
 * Processor for the {@link IDisplayIfProcessor} comment.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class DisplayIfProcessor
        extends AbstractCommentProcessor
        implements IDisplayIfProcessor {

    private List<P> paragraphsToBeRemoved = new ArrayList<>();
    private List<Tbl> tablesToBeRemoved = new ArrayList<>();
    private List<Tr> tableRowsToBeRemoved = new ArrayList<>();

    private DisplayIfProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    /**
     * Creates a new DisplayIfProcessor instance.
     *
     * @param pr the {@link PlaceholderReplacer} used for replacing expressions.
     *
     * @return a new DisplayIfProcessor instance.
     */
    public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
        return new DisplayIfProcessor(pr);
    }

    /** {@inheritDoc} */
    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        removeParagraphs();
        removeTables();
        removeTableRows();
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        paragraphsToBeRemoved = new ArrayList<>();
        tablesToBeRemoved = new ArrayList<>();
        tableRowsToBeRemoved = new ArrayList<>();
    }

    private void removeParagraphs() {
        for (P p : paragraphsToBeRemoved) {
            ObjectDeleter.deleteParagraph(p);
        }
    }

    private void removeTables() {
        for (Tbl table : tablesToBeRemoved) {
            ObjectDeleter.deleteTable(table);
        }
    }

    private void removeTableRows() {
        for (Tr row : tableRowsToBeRemoved) {
            ObjectDeleter.deleteTableRow(row);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void displayParagraphIf(Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        paragraphsToBeRemoved.add(getParagraph());
    }

    /** {@inheritDoc} */
    @Override
    public void displayParagraphIfPresent(Object condition) {
        displayParagraphIf(condition != null);
    }

    /** {@inheritDoc} */
    @Override
    public void displayTableIf(Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;

        P p = getParagraph();
        if (p.getParent() instanceof Tc tc
                && tc.getParent() instanceof Tr tr
                && tr.getParent() instanceof Tbl tbl
        ) {
            tablesToBeRemoved.add(tbl);
        }
        else throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(p)));
    }

    /** {@inheritDoc} */
    @Override
    public void displayTableRowIf(Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        P p = getParagraph();
        if (p.getParent() instanceof Tc tc && tc.getParent() instanceof Tr tr) {
            tableRowsToBeRemoved.add(tr);
        }
        else throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(p)));
    }
}
