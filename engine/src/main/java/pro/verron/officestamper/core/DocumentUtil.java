package pro.verron.officestamper.core;

import org.docx4j.XmlUtils;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.api.OfficeStamperException;

import static java.util.Collections.emptyList;

/// Utility class to retrieve elements from a document.
///
/// @author Joseph Verron
/// @author DallanMC
/// @version ${version}
/// @since 1.4.7
public class DocumentUtil {

    private static final Logger log = LoggerFactory.getLogger(DocumentUtil.class);

    private DocumentUtil() {
        throw new OfficeStamperException("Utility classes shouldn't be instantiated");
    }

    /// Finds the smallest common parent between two objects.
    ///
    /// @param o1 the first object
    /// @param o2 the second object
    ///
    /// @return the smallest common parent of the two objects
    ///
    /// @throws OfficeStamperException if there is an error finding the common parent
    public static ContentAccessor findSmallestCommonParent(Object o1, Object o2) {
        if (depthElementSearch(o1, o2) && o2 instanceof ContentAccessor contentAccessor)
            return findInsertableParent(contentAccessor);
        else if (o2 instanceof Child child) return findSmallestCommonParent(o1, child.getParent());
        else throw new OfficeStamperException();
    }

    /// Recursively searches for an element in a content tree.
    ///
    /// @param searchTarget the element to search for
    /// @param searchTree the content tree to search in
    ///
    /// @return true if the element is found, false otherwise
    public static boolean depthElementSearch(Object searchTarget, Object searchTree) {
        var element = XmlUtils.unwrap(searchTree);
        if (searchTarget.equals(element)) return true;

        var contentContent = switch (element) {
            case ContentAccessor accessor -> accessor.getContent();
            case SdtRun sdtRun -> sdtRun.getSdtContent()
                                        .getContent();
            case ProofErr _, Text _, R.CommentReference _, CommentRangeEnd _, CommentRangeStart _, Br _,
                 R.LastRenderedPageBreak _, CTBookmark _ -> emptyList();
            default -> {
                log.warn("Element {} not recognized", element);
                yield emptyList();
            }
        };

        return contentContent.stream()
                             .anyMatch(obj -> depthElementSearch(searchTarget, obj));
    }

    private static ContentAccessor findInsertableParent(Object searchFrom) {
        return switch (searchFrom) {
            case Tc tc -> tc;
            case Body body -> body;
            case Child child -> findInsertableParent(child.getParent());
            default -> throw new OfficeStamperException("Unexpected parent " + searchFrom.getClass());
        };
    }



}
