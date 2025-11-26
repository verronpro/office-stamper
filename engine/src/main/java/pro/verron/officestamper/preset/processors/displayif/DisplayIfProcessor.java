package pro.verron.officestamper.preset.processors.displayif;

import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.PlaceholderReplacer;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.utils.WmlUtils;

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
        extends CommentProcessor
        implements CommentProcessorFactory.IDisplayIfProcessor {

    private final List<Paragraph> paragraphsToBeRemoved = new ArrayList<>();
    private final List<Child> elementsToBeRemoved = new ArrayList<>();

    private DisplayIfProcessor(ProcessorContext processorContext, PlaceholderReplacer placeholderReplacer) {
        super(processorContext, placeholderReplacer);
    }

    /// Creates a new DisplayIfProcessor instance.
    ///
    /// @param pr the [PlaceholderReplacer] used for replacing expressions.
    ///
    /// @return a new DisplayIfProcessor instance.
    public static CommentProcessor newInstance(ProcessorContext processorContext, PlaceholderReplacer pr) {
        return new DisplayIfProcessor(processorContext, pr);
    }

    @Override
    public void displayParagraphIfAbsent(@Nullable Object condition) {
        displayParagraphIf(condition == null);
    }

    @Override
    public void displayParagraphIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        paragraphsToBeRemoved.add(paragraph());
        paragraphsToBeRemoved.forEach(Paragraph::remove);
        elementsToBeRemoved.forEach(WmlUtils::remove);
    }

    @Override
    public void displayParagraphIfPresent(@Nullable Object condition) {
        displayParagraphIf(condition != null);
    }


    @Override
    public void displayTableRowIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        var tr = paragraph().parent(Tr.class)
                            .orElseThrow(throwing("Paragraph is not within a row!"));
        elementsToBeRemoved.add(tr);
        paragraphsToBeRemoved.forEach(Paragraph::remove);
        elementsToBeRemoved.forEach(WmlUtils::remove);
    }

    @Override
    public void displayTableRowIfPresent(@Nullable Object condition) {
        displayTableRowIf(condition != null);
    }

    @Override
    public void displayTableRowIfAbsent(@Nullable Object condition) {
        displayTableRowIf(condition == null);
    }

    @Override
    public void displayTableIf(Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        var tbl = paragraph().parent(Tbl.class)
                             .orElseThrow(throwing("Paragraph is not within a table!"));
        elementsToBeRemoved.add(tbl);
        paragraphsToBeRemoved.forEach(Paragraph::remove);
        elementsToBeRemoved.forEach(WmlUtils::remove);
    }

    @Override
    public void displayTableIfPresent(@Nullable Object condition) {
        displayTableIf(condition != null);
    }

    @Override
    public void displayTableIfAbsent(@Nullable Object condition) {
        displayTableIf(condition == null);
    }

    @Override
    public void displayWordsIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        var start = comment().getCommentRangeStart();
        var end = comment().getCommentRangeEnd();
        var parent = (ContentAccessor) start.getParent();
        var startIndex = parent.getContent()
                               .indexOf(start);
        var iterator = parent.getContent()
                             .listIterator(startIndex);
        while (iterator.hasNext()) {
            var it = iterator.next();
            elementsToBeRemoved.add((Child) it);
            if (it.equals(end)) break;
        }
        paragraphsToBeRemoved.forEach(Paragraph::remove);
        elementsToBeRemoved.forEach(WmlUtils::remove);
    }

    @Override
    public void displayWordsIfPresent(@Nullable Object condition) {
        displayWordsIf(condition != null);
    }

    @Override
    public void displayWordsIfAbsent(@Nullable Object condition) {
        displayWordsIf(condition == null);
    }

    @Override
    public void displayDocPartIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        comment().getParent()
                 .getContent()
                 .removeAll(comment().getElements());
        paragraphsToBeRemoved.forEach(Paragraph::remove);
        elementsToBeRemoved.forEach(WmlUtils::remove);
    }

    @Override
    public void displayDocPartIfPresent(@Nullable Object condition) {
        displayDocPartIf(condition != null);
    }

    @Override
    public void displayDocPartIfAbsent(@Nullable Object condition) {
        displayDocPartIf(condition == null);
    }
}
