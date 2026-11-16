package pro.verron.officestamper.preset.processors.displayif;


import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.spi.Element;
import pro.verron.officestamper.spi.ProcessorContext;
import pro.verron.officestamper.preset.CommentProcessorFactory;

import java.util.ArrayList;

import static pro.verron.officestamper.api.OfficeStamperException.throwing;

/// Processor for the [CommentProcessorFactory.IDisplayIfProcessor] AnchoredComment.
///
/// @author Joseph Verron
/// @author Tom Hombergs
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
        // Materialize first, then remove: the slice iterator walks the live content lists, and
        // removing while iterating throws ConcurrentModificationException (legacy semantics).
        var toRemove = new ArrayList<Element>();
        var iterator = context().contentIterator();
        while (iterator.hasNext()) toRemove.add(iterator.next());
        toRemove.forEach(element -> element.parent()
                                           .content()
                                           .remove(element));
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
        comment().elements()
                 .forEach(element -> element.parent()
                                            .content()
                                            .remove(element));
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


