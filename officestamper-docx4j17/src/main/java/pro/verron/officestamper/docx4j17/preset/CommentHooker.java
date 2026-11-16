package pro.verron.officestamper.docx4j17.preset;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.spi.OfficeDocument;
import pro.verron.officestamper.spi.PreProcessor;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import pro.verron.officestamper.docx4j17.wml.WmlUtils;

import java.util.ArrayList;
import java.util.List;

import static pro.verron.officestamper.docx4j17.wml.WmlFactory.newCtAttr;
import static pro.verron.officestamper.docx4j17.wml.WmlFactory.newSmartTag;

/// The [CommentHooker] class is responsible for preparing comment processors in a Word pkg. It implements the
/// [PreProcessor] interface and provides functionality to process comment range starts and wrap them with smart tags
/// for further processing by the OfficeStamper engine.
///
/// This pre-processor is typically used to identify and mark comment-based expressions, making them recognizable as
/// hooks for subsequent processing steps.
public final class CommentHooker
        implements PreProcessor {

    /// Default constructor for CommentHooker.
    public CommentHooker() {
    }

    @Override
    public void process(OfficeDocument document) {
        var pkg = document.unwrap(WordprocessingMLPackage.class);
        var visitor = new CRSCollector();
        WmlUtils.visitDocument(pkg, visitor);
        // Replaces comment range starts with smart tags
        for (var commentRangeStart : visitor.commentRangeStarts()) {
            var parent = (ContentAccessor) commentRangeStart.getParent();
            var siblings = parent.getContent();
            var crsIndex = siblings.indexOf(commentRangeStart);
            var tag = newSmartTag("officestamper", newCtAttr("type", "processor"), commentRangeStart);
            siblings.set(crsIndex, tag);
        }
    }

    /// A collector class that gathers [CommentRangeStart] elements during pkg traversal. This class extends
    /// [TraversalUtilVisitor] to collect all [CommentRangeStart] objects encountered while traversing a DOCX pkg
    /// structure.
    public static class CRSCollector
            extends TraversalUtilVisitor<CommentRangeStart> {

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
        /// @return a list of CommentRangeStart objects that have been collected during pkg traversal
        public List<CommentRangeStart> commentRangeStarts() {
            return results;
        }
    }
}




