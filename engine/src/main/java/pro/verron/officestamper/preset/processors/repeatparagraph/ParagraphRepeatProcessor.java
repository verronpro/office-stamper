package pro.verron.officestamper.preset.processors.repeatparagraph;

import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.Hook;
import pro.verron.officestamper.core.SectionUtil;
import pro.verron.officestamper.core.TextualDocxPart;
import pro.verron.officestamper.preset.CommentProcessorFactory;

import java.util.LinkedList;

import static java.util.Collections.emptyIterator;
import static pro.verron.officestamper.core.SectionUtil.getPreviousSectionBreakIfPresent;
import static pro.verron.officestamper.core.SectionUtil.hasOddNumberOfSectionBreaks;

/// Class used internally to repeat document elements. Used by the lib, should not be instantiated by clients.
///
/// @author Joseph Verron
/// @author Youssouf Naciri
/// @version ${version}
/// @since 1.2.2
public class ParagraphRepeatProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IParagraphRepeatProcessor {

    private ParagraphRepeatProcessor(ProcessorContext processorContext) {
        super(processorContext);
    }

    /// Creates a new instance of [CommentProcessor] using the provided [PlaceholderReplacer].
    ///
    /// @return a new instance of [ParagraphRepeatProcessor].
    public static CommentProcessor newInstance(ProcessorContext processorContext) {
        return new ParagraphRepeatProcessor(processorContext);
    }

    @Override
    public void repeatParagraph(@Nullable Iterable<Object> objects) {
        var elements = comment().getElements();
        var parent = comment().getParent();
        var previousSectionBreak = getPreviousSectionBreakIfPresent(elements.getFirst(), parent);
        var oddNumberOfBreaks = hasOddNumberOfSectionBreaks(elements);
        var iterator = objects == null ? emptyIterator() : objects.iterator();
        var toRemove = elements.stream()
                               .filter(P.class::isInstance)
                               .map(P.class::cast)
                               .toList();

        var paragraphsToAdd = new LinkedList<P>();
        while (iterator.hasNext()) {
            var expressionContext = iterator.next();
            elements.stream()
                    .map(XmlUtils::deepCopy)
                    .forEach(clone -> {
                        if (clone instanceof ContentAccessor contentAccessor)
                            CommentUtil.deleteCommentFromElements(comment(), contentAccessor.getContent());
                        if (clone instanceof P p) {
                            var context = context();
                            var contextPart = context.part();
                            var part = new TextualDocxPart(contextPart.document());
                            var branch = context.branch();
                            var contextKey = branch.add(expressionContext);
                            Hook.ofHooks(p, part)
                                .forEachRemaining(hook -> hook.setContextKey(contextKey));
                            paragraphsToAdd.add(p);
                        }
                    });
            if (oddNumberOfBreaks && previousSectionBreak.isPresent() && iterator.hasNext()) {
                assert paragraphsToAdd.peekLast() != null : "There should be at least one ";
                SectionUtil.addSectionBreak(previousSectionBreak.get(), paragraphsToAdd.peekLast());
            }
        }
        paragraph().replace(toRemove, paragraphsToAdd);
    }
}
