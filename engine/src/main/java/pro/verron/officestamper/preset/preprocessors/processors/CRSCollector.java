package pro.verron.officestamper.preset.preprocessors.processors;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CommentRangeStart;

import java.util.ArrayList;
import java.util.List;

/// A collector class that gathers [CommentRangeStart] elements during document traversal. This class extends
/// [TraversalUtilVisitor] to collect all [CommentRangeStart] objects encountered while traversing a DOCX document
/// structure.
public class CRSCollector
        extends TraversalUtilVisitor<CommentRangeStart> {

    private final List<CommentRangeStart> results = new ArrayList<>();

    @Override
    public void apply(CommentRangeStart element) {
        results.add(element);

    }


    /// Returns the list of collected CommentRangeStart elements.
    ///
    /// @return a list of CommentRangeStart objects that have been collected during document traversal
    public List<CommentRangeStart> commentRangeStarts() {
        return results;
    }
}
