package pro.verron.officestamper.docx;

import org.jvnet.jaxb.lang.Child;

/// docx4j-flavor compatibility seam for JAXB parent tracking.
///
/// The parent-tracking interface for JAXB elements has a different fully-qualified
/// name in each docx4j flavor ({@code org.jvnet.jaxb2_commons.ppp.Child} for docx4j 11,
/// {@code org.jvnet.jaxb.lang.Child} for docx4j 17). Both interfaces declare exactly the
/// same operations, so the flavor-neutral sources compiled into both provider artifacts
/// route every parent-tracking access through this class, whose implementation is
/// provided once per provider (this file is the docx4j 17 implementation).
public final class JaxbCompat {

    private JaxbCompat() {
        throw new IllegalStateException("Utility class shouldn't be instantiated");
    }

    /// Returns the JAXB parent of the given element.
    ///
    /// @param child the element whose parent is requested
    /// @return the parent element, or {@code null} when the element is not
    ///         parent-tracked or has no parent
    public static Object parentOf(Object child) {
        if (child instanceof Child c) return c.getParent();
        return null;
    }

    /// Re-parents the given JAXB element.
    ///
    /// @param child  the element to re-parent; ignored when it is not parent-tracked
    /// @param parent the new parent element
    public static void setParent(Object child, Object parent) {
        if (child instanceof Child c) c.setParent(parent);
    }

    /// Tells whether the given object is a JAXB parent-tracked element.
    ///
    /// @param object the object to test
    /// @return {@code true} when the object implements the flavor's parent-tracking interface
    public static boolean isChild(Object object) {
        return object instanceof Child;
    }
}
