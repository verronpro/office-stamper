package pro.verron.officestamper.preset.preprocessors.similarrun;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.PreProcessor;

import java.util.LinkedHashSet;
import java.util.List;

public class MergeSameStyleRuns
        implements PreProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(WordprocessingMLPackage document) {
        var mainDocumentPart = document.getMainDocumentPart();
        var visitor = new SimilarRunVisitor();
        TraversalUtil.visit(mainDocumentPart, visitor);
        for (List<R> similarStyleRuns : visitor.getSimilarStyleRuns()) {
            R firstRun = similarStyleRuns.get(0);
            var runContent = firstRun.getContent();
            var firstRunContent = new LinkedHashSet<>(runContent);
            var firstRunParentContent = ((ContentAccessor) firstRun.getParent()).getContent();
            for (R r : similarStyleRuns.subList(1, similarStyleRuns.size())) {
                firstRunParentContent.remove(r);
                firstRunContent.addAll(r.getContent());
            }
            runContent.clear();
            runContent.addAll(firstRunContent);
        }
    }
}
