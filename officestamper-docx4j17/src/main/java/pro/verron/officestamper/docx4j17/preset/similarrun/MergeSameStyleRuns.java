package pro.verron.officestamper.docx4j17.preset.similarrun;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.PreProcessor;
import pro.verron.officestamper.docx4j17.wml.WmlUtils;

import java.util.LinkedHashSet;

/// Merges consecutive runs with the same styling into a single run.
///
/// This preprocessor analyzes the pkg and identifies adjacent runs that share identical styling properties. It
/// then merges these runs into a single run to reduce pkg complexity and improve processing efficiency.
///
/// The merging process preserves all content from the original runs while maintaining the formatting of the first run
/// in each sequence of similar runs.
///
/// @author Joseph Verron
public class MergeSameStyleRuns
        implements PreProcessor {

    @Override
    public void process(OfficeDocument document) {
        var pkg = document.unwrap(WordprocessingMLPackage.class);
        var visitor = new SimilarRunVisitor();
        WmlUtils.visitDocument(pkg, visitor);
        for (var similarStyleRuns : visitor.getSimilarStyleRuns()) {
            var firstRun = similarStyleRuns.getFirst();
            var runContent = firstRun.getContent();
            var firstRunContent = new LinkedHashSet<>(runContent);
            var firstRunParentContent = ((ContentAccessor) firstRun.getParent()).getContent();
            for (var r : similarStyleRuns.subList(1, similarStyleRuns.size())) {
                firstRunParentContent.remove(r);
                firstRunContent.addAll(r.getContent());
            }
            runContent.clear();
            runContent.addAll(firstRunContent);
        }
    }
}




