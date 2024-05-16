package org.wickedsource.docxstamper.processor.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.*;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.CommentUtil;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.docx4j.TextUtils.getText;

/**
 * Repeats a table row for each element in a list.
 *
 * @author Joseph Verron
 * @author Tom Hombergs
 * @version ${version}
 * @since 1.0.0
 */
public class RepeatProcessor
        extends AbstractCommentProcessor
        implements IRepeatProcessor {

    private final BiFunction<WordprocessingMLPackage, Tr, List<Tr>> nullSupplier;
    private Map<Tr, List<Object>> tableRowsToRepeat = new HashMap<>();
    private Map<Tr, Comment> tableRowsCommentsToRemove = new HashMap<>();

    private RepeatProcessor(
            ParagraphPlaceholderReplacer placeholderReplacer,
            BiFunction<WordprocessingMLPackage, Tr, List<Tr>> nullSupplier1
    ) {
        super(placeholderReplacer);
        nullSupplier = nullSupplier1;
    }

    /**
     * Creates a new RepeatProcessor.
     *
     * @param pr The PlaceholderReplacer to use.
     *
     * @return A new RepeatProcessor.
     */
    public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
        return new RepeatProcessor(pr, (document, row) -> emptyList());
    }

    /** {@inheritDoc} */
    @Override
    public void commitChanges(WordprocessingMLPackage document) {
        repeatRows(document);
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        this.tableRowsToRepeat = new HashMap<>();
        this.tableRowsCommentsToRemove = new HashMap<>();
    }

    private void repeatRows(final WordprocessingMLPackage document) {
        for (Map.Entry<Tr, List<Object>> entry : tableRowsToRepeat.entrySet()) {
            Tr row = entry.getKey();
            List<Object> expressionContexts = entry.getValue();

            Tbl table = (Tbl) XmlUtils.unwrap(row.getParent());
            int index = table.getContent()
                             .indexOf(row);


            List<Tr> changes;
            if (expressionContexts == null) {
                changes = nullSupplier.apply(document, row);
            }
            else {
                changes = new ArrayList<>();
                for (Object expressionContext : expressionContexts) {
                    Tr rowClone = XmlUtils.deepCopy(row);
                    Comment commentWrapper = Objects.requireNonNull(
                            tableRowsCommentsToRemove.get(row));
                    Comments.Comment comment = Objects.requireNonNull(commentWrapper.getComment());
                    BigInteger commentId = comment.getId();
                    CommentUtil.deleteCommentFromElements(rowClone.getContent(), commentId);
                    new ParagraphResolverDocumentWalker(rowClone,
                            expressionContext,
                            document,
                            this.placeholderReplacer).walk();
                    changes.add(rowClone);
                }
            }
            table.getContent()
                 .addAll(index + 1, changes);
            table.getContent()
                 .remove(row);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void repeatTableRow(List<Object> objects) {
        P p = getParagraph();
        if (p.getParent() instanceof Tc tc && tc.getParent() instanceof Tr tableRow) {
            tableRowsToRepeat.put(tableRow, objects);
            tableRowsCommentsToRemove.put(tableRow, getCurrentCommentWrapper());
        }
        else throw new OfficeStamperException(format("Paragraph is not within a table! : %s", getText(p)));
    }
}
