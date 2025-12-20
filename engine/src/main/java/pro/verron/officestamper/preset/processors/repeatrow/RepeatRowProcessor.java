package pro.verron.officestamper.preset.processors.repeatrow;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.CommentProcessor;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.ProcessorContext;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.Hook;
import pro.verron.officestamper.preset.CommentProcessorFactory;

import java.util.ArrayList;

/// Repeats a table row for each element in a list.
///
/// @author Joseph Verron
/// @author Tom Hombergs
/// @version ${version}
/// @since 1.0.0
public class RepeatRowProcessor
        extends CommentProcessor
        implements CommentProcessorFactory.IRepeatProcessor {


    public RepeatRowProcessor(ProcessorContext processorContext) {
        super(processorContext);
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

        if (objects == null) return;

        var changes = new ArrayList<Tr>();
        for (Object expressionContext : objects) {
            var rowClone = XmlUtils.deepCopy(row);
            CommentUtil.deleteCommentFromElements(comment(), rowClone.getContent());
            var contextKey = branch.add(expressionContext);
            Hook.ofHooks(rowClone, part)
                .forEachRemaining(hook -> hook.setContextKey(contextKey));
            changes.add(rowClone);
        }
        content.addAll(index, changes);
    }
}
