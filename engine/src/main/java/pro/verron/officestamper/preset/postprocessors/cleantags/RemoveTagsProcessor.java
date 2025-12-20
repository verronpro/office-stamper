package pro.verron.officestamper.preset.postprocessors.cleantags;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.api.PostProcessor;

import static pro.verron.officestamper.core.DocumentUtil.visitDocument;

/// A post-processor implementation that removes smart tags from a WordprocessingML document.
///
/// This processor is designed to clean up documents by removing specific smart tags while preserving their content. It
/// operates on the document's XML structure to find and eliminate smart tag elements.
public class RemoveTagsProcessor
        implements PostProcessor {
    private final String element;


    /// Constructs a new [RemoveTagsProcessor] with the specified element name.
    ///
    /// @param element the name of the element to be removed from the document
    public RemoveTagsProcessor(String element) {this.element = element;}

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new TagsVisitor(element);
        visitDocument(document, visitor);
        for (CTSmartTagRun tag : visitor.getTags()) {
            var parent = (ContentAccessor) tag.getParent();
            var siblings = parent.getContent();
            var index = siblings.indexOf(tag);
            siblings.remove(tag);
            siblings.addAll(index, tag.getContent());
        }
    }
}
