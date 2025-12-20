package pro.verron.officestamper.preset.preprocessors.processors;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CommentRangeStart;

import java.util.ArrayList;
import java.util.List;

public class CRSCollector
        extends TraversalUtilVisitor<CommentRangeStart> {

    private final List<CommentRangeStart> results = new ArrayList<>();

    @Override
    public void apply(CommentRangeStart element) {
        results.add(element);

    }

    public List<CommentRangeStart> commentRangeStarts() {
        return results;
    }
}
