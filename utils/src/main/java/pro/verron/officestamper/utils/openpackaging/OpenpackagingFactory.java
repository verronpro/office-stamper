package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.relationships.Relationship;
import pro.verron.officestamper.utils.UtilsException;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/// Utility class for creating Open Packaging objects.
///
/// This class provides helper methods to create instances of docx4j Open Packaging objects, wrapping checked exceptions
/// in runtime [UtilsException] for easier handling.
public class OpenpackagingFactory {

    private OpenpackagingFactory() {
        throw new UtilsException("Utility class shouldn't be instantiated");
    }

    /// Creates a new PartName instance from the given string representation.
    ///
    /// This method wraps the checked [InvalidFormatException] that can occur when creating a PartName in a runtime
    /// [UtilsException].
    ///
    /// @param partName the string representation of the part name
    ///
    /// @return a new PartName instance
    ///
    /// @throws UtilsException if the part name string is invalid
    public static PartName newPartName(String partName) {
        try {
            return new PartName(partName);
        } catch (InvalidFormatException e) {
            throw new UtilsException(e);
        }
    }

    public static Relationship newXmlPart(Part parts, byte[] imageBytes)
            throws Docx4JException {
        var imagePartName = new PartName("/word/media/image-%s.svg".formatted(UUID.randomUUID()));
        var imagePart = new DefaultXmlPart(imagePartName);
        imagePart.setRelationshipType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
        imagePart.setContentType(new ContentType("image/svg+xml"));
        imagePart.setDocument(new ByteArrayInputStream(imageBytes));
        return parts.addTargetPart(imagePart);
    }
}
