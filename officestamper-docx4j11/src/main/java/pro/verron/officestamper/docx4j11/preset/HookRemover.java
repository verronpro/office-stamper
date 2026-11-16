package pro.verron.officestamper.docx4j11.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.PostProcessor;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static pro.verron.officestamper.docx4j11.wml.WmlUtils.visitDocument;

/// A post-processor implementation that removes smart tags from a WordprocessingML pkg.
///
/// This processor is designed to clean up documents by removing specific smart tags (hooks) while preserving their
/// content. It operates on the pkg's XML structure to find and eliminate smart tag elements.
///
/// This is particularly useful for removing the `officestamper` smart tags used during the stamping process to leave a
/// clean pkg.
public final class HookRemover
        implements PostProcessor {
    private final String element;


    /// Constructs a new [HookRemover] with the specified element name.
    ///
    /// @param element the name of the element to be removed from the pkg
    public HookRemover(String element) {this.element = element;}

    @Override
    public void process(OfficeDocument document) {
        var pkg = document.unwrap(WordprocessingMLPackage.class);
        var visitor = new TagsVisitor(element);
        visitDocument(pkg, visitor);
        // Replaces tags with their content in parent
        for (CTSmartTagRun tag : visitor.getTags()) {
            var parent = (ContentAccessor) tag.getParent();
            var siblings = parent.getContent();
            var index = siblings.indexOf(tag);
            // The tag may have been unwrapped by a previous pass when the same
            // part was visited more than once (e.g. a header shared by several sections)
            if (index < 0) continue;
            siblings.remove(tag);
            siblings.addAll(index, tag.getContent());
        }
    }

    static class TagsVisitor
            extends TraversalUtilVisitor<CTSmartTagRun> {
        private final String element;
        private final List<CTSmartTagRun> results = new ArrayList<>();
        private final Set<CTSmartTagRun> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        TagsVisitor(String element) {this.element = element;}

        @Override
        public void apply(CTSmartTagRun tag) {
            if (element.equals(tag.getElement()) && seen.add(tag)) results.add(tag);
        }

        List<CTSmartTagRun> getTags() {
            return results;
        }
    }
}




