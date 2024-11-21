package pro.verron.officestamper.preset.processors.displayif;

import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.ObjectDeleter;
import pro.verron.officestamper.core.PlaceholderReplacer;
import pro.verron.officestamper.preset.CommentProcessorFactory;

import java.util.ArrayList;
import java.util.List;

import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// Processor for the [CommentProcessorFactory.IDisplayIfProcessor] comment.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class DisplayIfProcessor
        extends AbstractCommentProcessor
        implements CommentProcessorFactory.IDisplayIfProcessor {

    private List<Paragraph> paragraphsToBeRemoved = new ArrayList<>();
    private List<Tbl> tablesToBeRemoved = new ArrayList<>();
    private List<Tr> tableRowsToBeRemoved = new ArrayList<>();

    private DisplayIfProcessor(ParagraphPlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    /// Creates a new DisplayIfProcessor instance.
    ///
    /// @param pr the [PlaceholderReplacer] used for replacing expressions.
    ///
    /// @return a new DisplayIfProcessor instance.
    public static CommentProcessor newInstance(ParagraphPlaceholderReplacer pr) {
        return new DisplayIfProcessor(pr);
    }

    @Override public void commitChanges(DocxPart source) {
        removeParagraphs();
        removeTables();
        removeTableRows();
    }

    private void removeParagraphs() {
        paragraphsToBeRemoved.forEach(Paragraph::remove);
    }

    private void removeTables() {
        tablesToBeRemoved.forEach(ObjectDeleter::deleteTable);
    }

    private void removeTableRows() {
        tableRowsToBeRemoved.forEach(ObjectDeleter::deleteTableRow);
    }

    @Override public void reset() {
        paragraphsToBeRemoved = new ArrayList<>();
        tablesToBeRemoved = new ArrayList<>();
        tableRowsToBeRemoved = new ArrayList<>();
    }

    @Override public void displayParagraphIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        paragraphsToBeRemoved.add(this.getParagraph());
    }

    @Override public void displayParagraphIfPresent(@Nullable Object condition) {
        displayParagraphIf(condition != null);
    }

    @Override public void displayTableRowIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        var tr = this.getParagraph()
                     .parent(Tr.class)
                     .orElseThrow(throwing("Paragraph is not within a row!"));
        tableRowsToBeRemoved.add(tr);
    }

    @Override public void displayTableRowIfPresent(@Nullable Object condition) {
        displayTableRowIf(condition != null);
    }

    @Override public void displayTableIf(Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        var tbl = this.getParagraph()
                      .parent(Tbl.class)
                      .orElseThrow(throwing("Paragraph is not within a table!"));
        tablesToBeRemoved.add(tbl);
    }

    @Override public void displayTableIfPresent(@Nullable Object condition) {
        displayTableIf(condition != null);
    }

    @Override public void displayWordsIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        var commentWrapper = getCurrentCommentWrapper();
        commentWrapper.getParent()
                      .getContent()
                      .removeAll(commentWrapper.getElements());
    }

    @Override public void displayWordsIfPresent(@Nullable Object condition) {
        displayWordsIf(condition != null);
    }

    @Override public void displayDocPartIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        var commentWrapper = getCurrentCommentWrapper();
        commentWrapper.getParent()
                      .getContent()
                      .removeAll(commentWrapper.getElements());
    }

    @Override public void displayDocPartIfPresent(@Nullable Object condition) {
        displayDocPartIf(condition != null);
    }
}
