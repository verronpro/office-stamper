package pro.verron.officestamper.preset.processors.replacewith;

import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.AbstractProcessor;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ParagraphPlaceholderReplacer;
import pro.verron.officestamper.api.Processor;
import pro.verron.officestamper.preset.ProcessorFactory;

import java.util.List;
import java.util.function.Function;

import static pro.verron.officestamper.utils.WmlFactory.newText;

/// Processor that replaces the current run with the provided expression.
/// This is useful for replacing an expression in a comment with the result of the expression.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.7
public class ReplaceWithProcessor
        extends AbstractProcessor
        implements ProcessorFactory.IReplaceWithProcessor {

    private static final Logger log = LoggerFactory.getLogger(ReplaceWithProcessor.class);

    private final Function<R, List<Object>> nullSupplier;

    private ReplaceWithProcessor(
            ParagraphPlaceholderReplacer placeholderReplacer,
            Function<R, List<Object>> nullSupplier
    ) {
        super(placeholderReplacer);
        this.nullSupplier = nullSupplier;
    }

    /// Creates a new processor that replaces the current run with the result of the expression.
    ///
    /// @param pr the placeholder replacer to use
    ///
    /// @return the processor
    public static Processor newInstance(ParagraphPlaceholderReplacer pr) {
        return new ReplaceWithProcessor(pr, R::getContent);
    }

    @Override
    public void commitChanges(DocxPart document) {
        // nothing to commit
    }

    @Override
    public void reset() {
        // nothing to reset
    }

    @Override
    public void replaceWordWith(@Nullable String expression) {
        R run = this.getCurrentRun();
        if (run == null) { // TODO Find a way to never call this method when not on a run comment
            log.info("Impossible to put expression {} in a null run", expression);
            return;
        }

        var target = expression != null ? List.of(newText(expression)) : nullSupplier.apply(run);

        run.getContent()
           .clear();
        run.getContent()
           .addAll(target);
    }
}
