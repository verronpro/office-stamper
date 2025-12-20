package pro.verron.officestamper.preset.postprocessors.cleantags;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTSmartTagRun;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.api.PostProcessor;

import static pro.verron.officestamper.core.DocumentUtil.visitDocument;

public class RemoveTagsProcessor
        implements PostProcessor {
    private final String element;

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
