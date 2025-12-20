package pro.verron.officestamper.preset.preprocessors.processors;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.api.PreProcessor;
import pro.verron.officestamper.core.DocumentUtil;

import static pro.verron.officestamper.utils.wml.WmlFactory.newCtAttr;
import static pro.verron.officestamper.utils.wml.WmlFactory.newSmartTag;

/// The [PrepareCommentProcessors] class is responsible for preparing comment processors in a Word document. It
/// implements the [PreProcessor] interface and provides functionality to process comment range starts and wrap them
/// with smart tags for further processing by the OfficeStamper engine.
public class PrepareCommentProcessors
        implements PreProcessor {
    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new CRSCollector();
        DocumentUtil.visitDocument(document, visitor);
        for (var commentRangeStart : visitor.commentRangeStarts()) {
            var parent = (ContentAccessor) commentRangeStart.getParent();
            var siblings = parent.getContent();
            var crsIndex = siblings.indexOf(commentRangeStart);
            var tag = newSmartTag("officestamper", newCtAttr("type", "cProcessor"), commentRangeStart);
            siblings.set(crsIndex, tag);
        }
    }
}
