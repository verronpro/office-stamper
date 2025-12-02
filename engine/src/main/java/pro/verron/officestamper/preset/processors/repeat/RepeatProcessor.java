package pro.verron.officestamper.preset.processors.repeat;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.*;
import pro.verron.officestamper.core.CommentUtil;
import pro.verron.officestamper.core.DocxIterator;
import pro.verron.officestamper.preset.CommentProcessorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

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
    private final Map<Tr, Iterable<Object>> tableRowsToRepeat = new HashMap<>();
    private final Map<Tr, Comment> tableRowsCommentsToRemove = new HashMap<>();

    private RepeatProcessor(
            ProcessorContext processorContext,
            BiFunction<WordprocessingMLPackage, Tr, List<Tr>> nullSupplier1
    ) {
        super(processorContext);
        nullSupplier = nullSupplier1;
    }


    /// Creates a new [RepeatProcessor] instance.
    ///
    /// @param processorContext The [ProcessorContext] to use for processing.
    ///
    /// @return A new [RepeatProcessor] instance.
    public static CommentProcessor newInstance(ProcessorContext processorContext) {
        return new RepeatProcessor(processorContext, (_, _) -> emptyList());
    }

    /// {@inheritDoc}
    @Override
    public void repeatTableRow(@Nullable Iterable<Object> objects) {
        var tr = paragraph().parent(Tr.class)
                            .orElseThrow(OfficeStamperException.throwing("This paragraph is not in a table row."));
        tableRowsToRepeat.put(tr, objects);
        tableRowsCommentsToRemove.put(tr, comment());
        repeatRows(context().part());
    }

    private void repeatRows(DocxPart source) {
        for (Map.Entry<Tr, Iterable<Object>> entry : tableRowsToRepeat.entrySet()) {
            Tr row = entry.getKey();
            Iterable<Object> expressionContexts = entry.getValue();

            Tbl table = (Tbl) XmlUtils.unwrap(row.getParent());
            var content = table.getContent();
            int index = content.indexOf(row);
            content.remove(row);

            List<Tr> changes;
            if (expressionContexts == null) {
                changes = nullSupplier.apply(source.document(), row);
            }
            else {
                changes = new ArrayList<>();
                for (Object expressionContext : expressionContexts) {
                    Tr rowClone = XmlUtils.deepCopy(row);
                    Comment commentWrapper = requireNonNull(tableRowsCommentsToRemove.get(row));
                    CommentUtil.deleteCommentFromElements(commentWrapper, rowClone.getContent());
                    var tagIterator = DocxIterator.ofTags(rowClone, source);
                    while (tagIterator.hasNext()) {
                        var tag = tagIterator.next();
                        if (tag.type()
                               .filter("placeholder"::equals)
                               .isPresent()) {
                            tag.setContextReference(context().branch()
                                                             .add(expressionContext));
                        }
                    }
                    changes.add(rowClone);
                }
            }
            content.addAll(index, changes);
        }
    }
}
