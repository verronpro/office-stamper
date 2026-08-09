package pro.verron.officestamper.preset.processors.displayif;


import org.jspecify.annotations.Nullable;
import org.jvnet.jaxb.lang.Child;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.utils.wml.Content;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.ArrayList;

import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// Processor for the [CommentProcessorFactory.IDisplayIfProcessor] comment.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @since 1.0.0
public class DisplayIfProcessor extends CommentProcessor implements CommentProcessorFactory.IDisplayIfProcessor {


    /// Creates a new [DisplayIfProcessor].
    ///
    /// @param processorContext the context in which this processor runs
    public DisplayIfProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void displayParagraphIfAbsent(@Nullable Object condition) {
        displayParagraphIf(condition == null);
    }

    @Override
    public void displayParagraphIf(@Nullable Boolean condition) {
        var paragraph = context().paragraph();
        if (!Boolean.TRUE.equals(condition)) paragraph.remove();
        paragraph.apply(a -> new Content(a).deleteCommentFromElements(comment().getId()));
    }

    @Override
    public void displayParagraphIfPresent(@Nullable Object condition) {
        displayParagraphIf(condition != null);
    }

    @Override
    public void displayTableRowIf(@Nullable Boolean condition) {
        var tableRow = context().tableRow().orElseThrow(throwing("Paragraph is not within a row!"));
        if (!Boolean.TRUE.equals(condition)) tableRow.remove();
        new Content(tableRow.asTr()).deleteCommentFromElements(comment().getId());
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
    public void displayTableIf(@Nullable Boolean condition) {
        var table = context().table().orElseThrow(throwing("Paragraph is not within a table!"));
        if (!Boolean.TRUE.equals(condition)) table.remove();
        new Content(table.asTbl()).deleteCommentFromElements(comment().getId());
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
        if (!Boolean.TRUE.equals(condition)) {
            var iterator = context().contentIterator();
            var toRemove = new ArrayList<Child>();
            while (iterator.hasNext()) {
                var it = iterator.next();
                toRemove.add((Child) it);
            }
            toRemove.forEach(WmlUtils::remove);
        }
        context().comment().getContent().deleteCommentFromElements(comment().getId());
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
        var content = comment().getContent();
        var items = content.getContent();
        if (!Boolean.TRUE.equals(condition)) items.removeAll(comment().getElements());
        content.deleteCommentFromElements(comment().getId());
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
