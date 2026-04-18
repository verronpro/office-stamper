package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.image.ImgPart;
import pro.verron.officestamper.utils.svg.SvgUtils;

import java.io.ByteArrayInputStream;

import static org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImageName;
import static pro.verron.officestamper.utils.image.ImgUtils.detectFormat;
import static pro.verron.officestamper.utils.image.ImgUtils.supportedContentType;
import static pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils.*;

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

    public static ImgPart newImgPart(OpcPackage opcPackage, Part sourcePart, byte[] bytes) {
        if (bytes.length == 0) throw new UtilsException("Can't create image from empty byte array");

        var optFormat = detectFormat(bytes);
        var format = optFormat.orElseThrow(() -> new UtilsException("Could not detect a supported image type."));

        var optMimeType = supportedContentType(format.name());
        var mimeType = optMimeType.orElseThrow(() -> new UtilsException("Unsupported image type"));

        ensureHasRelationshipPart(sourcePart);
        var relationshipId = createRelationshipId(sourcePart);
        var partName = createImageName(opcPackage, sourcePart, relationshipId, format.name());
        var ctm = opcPackage.getContentTypeManager();

        Part imgPart;
        if (mimeType.equals(ContentTypes.IMAGE_SVG)) {
            var document = SvgUtils.parseDocument(bytes);
            imgPart = createSvgPart(document, ctm, partName);
        }
        else imgPart = createImagePart(bytes, ctm, mimeType, partName);
        var relationship = setupRelationship(sourcePart, imgPart, relationshipId);
        return new ImgPart(format, relationship);
    }

    static Part createImagePart(byte[] bytes, ContentTypeManager ctm, String mimeType, String partName) {
        try {
            var imagePart = (BinaryPartAbstractImage) ctm.newPartForContentType(mimeType, partName, null);
            imagePart.setBinaryData(new ByteArrayInputStream(bytes));
            return imagePart;
        } catch (InvalidFormatException | PartUnrecognisedException e) {
            throw new UtilsException(e);
        }
    }

    static Relationship setupRelationship(Part sourcePart, Part targetPart, String relationshipId) {
        try {
            var reuseExisting = RelationshipsPart.AddPartBehaviour.REUSE_EXISTING;
            return sourcePart.addTargetPart(targetPart, reuseExisting, relationshipId);
        } catch (InvalidFormatException e) {
            throw new UtilsException(e);
        }
    }
}
