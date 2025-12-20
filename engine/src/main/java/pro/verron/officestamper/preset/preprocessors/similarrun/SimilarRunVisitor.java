package pro.verron.officestamper.preset.preprocessors.similarrun;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A visitor implementation for traversing and identifying runs with similar styling in a DOCX document. This class
/// extends [TraversalUtilVisitor] to process Run elements ([R]) and group consecutive runs that share the same
/// formatting properties together.
public class SimilarRunVisitor
        extends TraversalUtilVisitor<R> {

    private final List<List<R>> similarStyleRuns = new ArrayList<>();


    /// Retrieves the list of grouped runs that have similar styling. Each inner list contains consecutive runs that
    /// share the same formatting properties.
    ///
    /// @return A list of lists, where each inner list contains runs with similar styling.
    public List<List<R>> getSimilarStyleRuns() {
        return similarStyleRuns;
    }

    @Override
    public void apply(R element, Object parent, List<Object> siblings) {
        var rPr = element.getRPr();
        var currentIndex = siblings.indexOf(element);
        var similarRuns = siblings.stream()
                                  .skip(currentIndex)
                                  .takeWhile(o -> o instanceof R run && Objects.equals(run.getRPr(), rPr))
                                  .map(R.class::cast)
                                  .toList();
        if (similarRuns.size() > 1) similarStyleRuns.add(similarRuns);
    }
}
