package pro.verron.officestamper.test;

import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.Paragraph;
import pro.verron.officestamper.api.PlaceholderReplacer;
import pro.verron.officestamper.api.ProcessorContext;

import java.util.ArrayList;
import java.util.List;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/// This is an example of a custom [CommentProcessor] implementation.
///
/// Users of the docx-stamper library could use it to understand how they could
/// leverage the library to create their own custom comment processors.
///
/// Specifically, it's designed to replace each paragraph that has been
/// commented with the annotation "visitParagraph" exposed by the
/// [ICustomCommentProcessor#visitParagraph()] public method,
/// marking it with the text 'Visited' in the resultant stamped Word
/// document.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.6
public class CustomCommentProcessor
        extends CommentProcessor
        implements ICustomCommentProcessor {

    private static final List<Paragraph> visitedParagraphs = new ArrayList<>();

    /// Constructor for CustomCommentProcessor.
    ///
    /// @param placeholderReplacer a [PlaceholderReplacer] object
    public CustomCommentProcessor(ProcessorContext processorContext, PlaceholderReplacer placeholderReplacer) {
        super(processorContext, placeholderReplacer);
    }

    public static CommentProcessor newInstance(
            ProcessorContext processorContext,
            PlaceholderReplacer placeholderReplacer
    ) {
        return new CustomCommentProcessor(processorContext, placeholderReplacer);
    }

    /// {@inheritDoc}
    @Override
    public void visitParagraph() {
        visitedParagraphs.add(paragraph());
        visitedParagraphs.forEach(para -> para.apply((ContentAccessor p) -> {
            var content = p.getContent();
            content.clear();
            content.add(newRun("Visited"));
        }));
    }
}
