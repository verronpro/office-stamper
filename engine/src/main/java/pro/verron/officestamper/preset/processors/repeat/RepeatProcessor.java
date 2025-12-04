package pro.verron.officestamper.preset.processors.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.DocxIterator;
import pro.verron.officestamper.preset.CommentProcessorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static java.util.Collections.emptyList;

/// Repeats a table row for each element in a list.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class RepeatProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IRepeatProcessor {

    private final BiFunction<WordprocessingMLPackage, Tr, List<Tr>> nullSupplier;

    private RepeatProcessor(
            ProcessorContext processorContext,
            BiFunction<WordprocessingMLPackage, Tr, List<Tr>> nullSupplier
    ) {
        super(processorContext);
        this.nullSupplier = nullSupplier;
    }


    /// Creates a new [RepeatProcessor] instance.
    ///
    /// @param processorContext The [ProcessorContext] to use for processing.
    ///
    /// @return A new [RepeatProcessor] instance.
    public static CommentProcessor newInstance(ProcessorContext processorContext) {
        return new RepeatProcessor(processorContext, (_, _) -> emptyList());
    }

    @Override
    public void repeatTableRow(@Nullable Iterable<Object> objects) {
        var context = context();
        var part = context.part();
        var branch = context.branch();
        var paragraph = paragraph();
        var row = paragraph.parent(Tr.class)
                           .orElseThrow(OfficeStamperException.throwing("This paragraph is not in a table row."));

        var table = (Tbl) XmlUtils.unwrap(row.getParent());
        var content = table.getContent();
        int index = content.indexOf(row);
        content.remove(row);


        if (objects == null) {
            var changes = nullSupplier.apply(part.document(), row);
            content.addAll(index, changes);
            return;
        }

        var changes = new ArrayList<Tr>();
        for (Object expressionContext : objects) {
            var rowClone = XmlUtils.deepCopy(row);
            CommentUtil.deleteCommentFromElements(comment(), rowClone.getContent());
            var contextIndex = branch.add(expressionContext);
            DocxIterator.ofHooks(rowClone, part)
                        .forEachRemaining(hook -> hook.ifPresent(h -> h.setContextReference(contextIndex)));
            changes.add(rowClone);
        }
        content.addAll(index, changes);

    }
}
