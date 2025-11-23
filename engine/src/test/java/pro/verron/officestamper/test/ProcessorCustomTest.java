package pro.verron.officestamper.test;

import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.*;

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
public class ProcessorCustomTest
        extends AbstractCommentProcessor
        implements ICustomCommentProcessor {

    private static final List<Paragraph> visitedParagraphs = new ArrayList<>();

    /// Constructor for ProcessorCustomTest.
    ///
    /// @param placeholderReplacer a [ParagraphPlaceholderReplacer] object
    public ProcessorCustomTest(ParagraphPlaceholderReplacer placeholderReplacer) {
        super(placeholderReplacer);
    }

    /// {@inheritDoc}
    @Override
    public void commitChanges(DocxPart document) {
        visitedParagraphs.forEach(para -> para.apply((ContentAccessor p)->{
            var content = p.getContent();
            content.clear();
            content.add(newRun("Visited"));
        }));
    }

    /// {@inheritDoc}
    @Override
    public void reset() {
        // DO NOTHING
    }

    /// {@inheritDoc}
    @Override
    public void setCurrentCommentWrapper(Comment comment) {
        // DO NOTHING
    }

    @Override public void setParagraph(Paragraph paragraph) {
        super.setParagraph(paragraph);
    }

    /// {@inheritDoc}
    @Override
    public void setCurrentRun(R run) {
        // DO NOTHING
    }

    /// {@inheritDoc}
    @Override
    public void visitParagraph() {
        visitedParagraphs.add(getParagraph());
    }
}
