package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import pro.verron.officestamper.utils.UtilsException;

import java.io.ByteArrayInputStream;

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

    /// Creates an image part using the specified binary data, content type manager, MIME type, and part name.
    ///
    /// This method generates an image part for use in Open Packaging workflows. It wraps checked exceptions such as
    /// InvalidFormatException and PartUnrecognisedException in a runtime UtilsException for easier handling.
    ///
    /// @param ctm the content type manager to use for part creation
    /// @param bytes the binary data for the image
    /// @param mimeType the MIME type of the image (e.g., "image/png")
    /// @param partName the name of the part to be created
    ///
    /// @return the created image part
    ///
    /// @throws UtilsException if an error occurs while creating the part
    public static Part createImagePart(ContentTypeManager ctm, byte[] bytes, String mimeType, String partName) {
        try {
            var imagePart = (BinaryPartAbstractImage) ctm.newPartForContentType(mimeType, partName, null);
            imagePart.setBinaryData(new ByteArrayInputStream(bytes));
            return imagePart;
        } catch (InvalidFormatException | PartUnrecognisedException e) {
            throw new UtilsException(e);
        }
    }

    /// Establishes a relationship between a source part and a target part using the specified relationship ID.
    ///
    /// @param sourcePart the source part from which the relationship originates
    /// @param targetPart the target part to which the relationship points
    /// @param relationshipId the unique identifier for the relationship
    /// @return the created relationship between the source and target parts
    /// @throws UtilsException if an error occurs while creating the relationship
    public static Relationship setupRelationship(Part sourcePart, Part targetPart, String relationshipId) {
        try {
            var reuseExisting = RelationshipsPart.AddPartBehaviour.REUSE_EXISTING;
            return sourcePart.addTargetPart(targetPart, reuseExisting, relationshipId);
        } catch (InvalidFormatException e) {
            throw new UtilsException(e);
        }
    }
}
