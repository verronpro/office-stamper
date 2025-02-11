package pro.verron.officestamper.preset.preprocessors.similarrun;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.R;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.core.DocumentUtil;

import java.util.LinkedHashSet;
import java.util.List;

/// Merge consecutive runs sharing the same style within a WordprocessingMLPackage
/// document.
///
/// This class processes the document before any primary processing to
/// streamline content and reduce redundancy by consolidating similar-styled runs
/// into a single run.
/// ## Processing Steps
/// - This class uses a SimilarRunVisitor to identify groups of consecutive
///   runs that share the same style properties.
/// - The identified similar style runs contents are merged into the first run of the group.
/// - Then the later runs in the group are removed from their parent content.
/// The merging behavior ensures that styled text runs are simplified, reducing the
/// complexity of the document's structure without altering its visual content.
/// ## Use Case
/// This class is especially useful in scenarios where documents are generated or
/// edited programmatically, and redundant runs with similar styles are inadvertently
/// created.
/// It improves document consistency and maintainability by reducing the amount elements in the document markup.
public class MergeSameStyleRuns
        implements PreProcessor {

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new SimilarRunVisitor();
        DocumentUtil.visitDocument(document, visitor);
        for (List<R> similarStyleRuns : visitor.getSimilarStyleRuns()) {
            R firstRun = similarStyleRuns.getFirst();
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
