package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.*;
import pro.verron.officestamper.utils.wml.WmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static pro.verron.officestamper.utils.wml.WmlFactory.newCtAttr;
import static pro.verron.officestamper.utils.wml.WmlFactory.newSmartTag;

/// The [CommentHooker] class is responsible for preparing comment processors in a Word document. It implements the
/// [PreProcessor] interface and provides functionality to process comment range starts and wrap them with smart tags
/// for further processing by the OfficeStamper engine.
///
/// This pre-processor is typically used to identify and mark comment-based expressions, making them recognizable as
/// hooks for subsequent processing steps.
public final class CommentHooker implements PreProcessor {

    /// Default constructor for CommentHooker.
    public CommentHooker() {
    }

    @Override
    public void process(WordprocessingMLPackage document) {
        var visitor = new CRSCollector();
        WmlUtils.visitDocument(document, visitor);
        // Replaces comment range starts with smart tags
        for (var commentRangeStart : visitor.commentRangeStarts()) {
            var parent = (ContentAccessor) Objects.requireNonNull(commentRangeStart.getParent());
            // Google Docs style comment range starts
            if (parent instanceof CTSdtContentRun sdtContent && sdtContent.getContent()
                    .size() == 1 && sdtContent.getParent() instanceof SdtRun sdtRun) {
                var runTag = sdtRun.getSdtPr().getTag();
                parent = (P) sdtRun.getParent();
                var siblings = parent.getContent();
                var crsIndex = siblings.indexOf(sdtRun);
                var attributes = List.of(newCtAttr("type", "processor"), newCtAttr("sdtruntag", runTag.getVal()));
                var tag = newSmartTag("officestamper", attributes, commentRangeStart);
                siblings.set(crsIndex, tag);
            } else { // Word style comment range starts
                var siblings = parent.getContent();
                var crsIndex = siblings.indexOf(commentRangeStart);
                var attributes = List.of(newCtAttr("type", "processor"));
                var tag = newSmartTag("officestamper", attributes, commentRangeStart);
                siblings.set(crsIndex, tag);
            }

        }
    }

    /// A collector class that gathers [CommentRangeStart] elements during document traversal. This class extends
    /// [TraversalUtilVisitor] to collect all [CommentRangeStart] objects encountered while traversing a DOCX document
    /// structure.
    public static class CRSCollector extends TraversalUtilVisitor<CommentRangeStart> {

        private final List<CommentRangeStart> results = new ArrayList<>();

        /// Default constructor for CRSCollector.
        public CRSCollector() {
        }

        @Override
        public void apply(CommentRangeStart element) {
            results.add(element);
        }

        /// Returns the list of collected CommentRangeStart elements.
        ///
        /// @return a list of CommentRangeStart objects that have been collected during document traversal
        public List<CommentRangeStart> commentRangeStarts() {
            return results;
        }
    }
}
