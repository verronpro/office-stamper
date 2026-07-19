package pro.verron.officestamper.api;

import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.utils.wml.DocxDocument;

import java.util.ArrayList;
import java.util.List;

import static pro.verron.officestamper.utils.wml.WmlFactory.newSdtRun;
import static pro.verron.officestamper.utils.wml.WmlUtils.visitDocument;

/// A post-processor implementation that removes smart tags from a WordprocessingML document.
///
/// This processor is designed to clean up documents by removing specific smart tags (hooks) while preserving their
/// content. It operates on the document's XML structure to find and eliminate smart tag elements.
///
/// This is particularly useful for removing the `officestamper` smart tags used during the stamping process to leave a
/// clean document.
public final class HookRemover implements PostProcessor {
    private final String element;


    /// Constructs a new [HookRemover] with the specified element name.
    ///
    /// @param element the name of the element to be removed from the document
    public HookRemover(String element) {
        this.element = element;
    }

    @Override
    public void process(DocxDocument document) {
        var visitor = new TagsVisitor(element);
        visitDocument(document, visitor);
        // Replaces tags with their content in parent
        for (CTSmartTagRun tag : visitor.getTags()) {
            var parent = (ContentAccessor) tag.getParent();
            var siblings = parent.getContent();
            var index = siblings.indexOf(tag);
            siblings.remove(tag);
            var smartTagPr = tag.getSmartTagPr();
            var smartTagPrAttr = smartTagPr.getAttr();
            var optionalSdtruntag = smartTagPrAttr.stream()
                    .filter(attr -> attr.getName().equals("sdtruntag"))
                    .findFirst();
            if (!tag.getContent().isEmpty() && optionalSdtruntag.isPresent()) {
                var sdtRunTag = optionalSdtruntag.get().getVal();
                siblings.add(index, newSdtRun(sdtRunTag, tag.getContent()));
            } else {
                siblings.addAll(index, tag.getContent());
            }
        }
    }

    static class TagsVisitor extends TraversalUtilVisitor<CTSmartTagRun> {
        private final String element;
        private final List<CTSmartTagRun> results = new ArrayList<>();

        TagsVisitor(String element) {
            this.element = element;
        }

        @Override
        public void apply(CTSmartTagRun tag) {
            if (element.equals(tag.getElement())) results.add(tag);
        }

        List<CTSmartTagRun> getTags() {
            return results;
        }
    }
}
