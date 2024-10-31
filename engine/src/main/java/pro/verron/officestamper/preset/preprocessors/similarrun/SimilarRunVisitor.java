package pro.verron.officestamper.preset.preprocessors.similarrun;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SimilarRunVisitor
        extends TraversalUtilVisitor<R> {

    private final List<List<R>> similarStyleRuns = new ArrayList<>();

    public List<List<R>> getSimilarStyleRuns() {
        return similarStyleRuns;
    }

    @Override
    public void apply(R element, Object parent, List<Object> siblings) {
        RPr rPr = element.getRPr();
        int currentIndex = siblings.indexOf(element);
        List<R> similarStyleConcurrentRun = siblings
                .stream()
                .skip(currentIndex)
                .takeWhile(o -> o instanceof R run && Objects.equals(run.getRPr(), rPr))
                .map(R.class::cast)
                .toList();

        if (similarStyleConcurrentRun.size() > 1)
            similarStyleRuns.add(similarStyleConcurrentRun);
    }
}
