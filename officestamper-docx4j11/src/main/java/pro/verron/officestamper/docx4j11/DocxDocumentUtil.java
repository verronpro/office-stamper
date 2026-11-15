package pro.verron.officestamper.docx4j11;

import org.docx4j.XmlUtils;
import org.docx4j.wml.Body;
import org.docx4j.wml.Br;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.ProofErr;
import org.docx4j.wml.R.CommentReference;
import org.docx4j.wml.R.LastRenderedPageBreak;
import org.docx4j.wml.SdtRun;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.jvnet.jaxb2_commons.ppp.Child;

import java.util.List;

import static java.util.Collections.emptyList;

/// Element-tree search helpers for the docx4j 11 provider (port of the engine's `DocumentUtil`).
public final class DocxDocumentUtil {

    private DocxDocumentUtil() {
    }

    /// Finds the smallest common parent between two objects — the insertable content holder that
    /// contains both (port of the legacy `DocumentUtil.findSmallestCommonParent`).
    public static ContentAccessor findSmallestCommonParent(Object o1, Object o2) {
        if (depthElementSearch(o1, o2) && o2 instanceof ContentAccessor contentAccessor) {
            return findInsertableParent(contentAccessor);
        }
        if (o2 instanceof Child child) return findSmallestCommonParent(o1, child.getParent());
        throw new IllegalStateException("Could not find a common parent for " + o1 + " and " + o2);
    }

    /// Recursively searches for an element in a content tree (port of the legacy
    /// `DocumentUtil.depthElementSearch`).
    static boolean depthElementSearch(Object searchTarget, Object searchTree) {
        var element = XmlUtils.unwrap(searchTree);
        if (searchTarget.equals(element)) return true;

        var content = switch (element) {
            case ContentAccessor accessor -> accessor.getContent();
            case SdtRun sdtRun -> sdtRun.getSdtContent().getContent();
            // Leaf element types carry no searchable content
            case ProofErr _, Text _, CommentReference _, CommentRangeEnd _, CommentRangeStart _, Br _,
                 LastRenderedPageBreak _, CTBookmark _ -> emptyList();
            default -> emptyList();
        };

        return content.stream().anyMatch(obj -> depthElementSearch(searchTarget, obj));
    }

    /// Walks up the tree from the given object to the nearest insertable parent — a cell or the body.
    static ContentAccessor findInsertableParent(Object searchFrom) {
        return switch (searchFrom) {
            case Tc tc -> tc;
            case Tbl _ when searchFrom instanceof Child child -> findInsertableParent(child.getParent());
            case Body body -> body;
            case Child child -> findInsertableParent(child.getParent());
            default -> throw new IllegalStateException("Unexpected parent " + searchFrom.getClass());
        };
    }
}
