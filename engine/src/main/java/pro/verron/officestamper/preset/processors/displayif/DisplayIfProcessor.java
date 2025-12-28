package pro.verron.officestamper.preset.processors.displayif;


import org.jspecify.annotations.Nullable;
import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.ArrayList;

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
        if (Boolean.TRUE.equals(condition)) return;
        context().paragraph()
                 .remove();
    }

    @Override
    public void displayParagraphIfPresent(@Nullable Object condition) {
        displayParagraphIf(condition != null);
    }

    @Override
    public void displayTableRowIf(@Nullable Boolean condition) {
        if (Boolean.TRUE.equals(condition)) return;
        context().tableRow()
                 .orElseThrow(throwing("Paragraph is not within a row!"))
                 .remove();
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
        if (Boolean.TRUE.equals(condition)) return;
        context().table()
                 .orElseThrow(throwing("Paragraph is not within a table!"))
                 .remove();
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
        var iterator = context().contentIterator();
        var toRemove = new ArrayList<Child>();
        while (iterator.hasNext()) {
            var it = iterator.next();
            toRemove.add((Child) it);
        }
        toRemove.forEach(WmlUtils::remove);
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
