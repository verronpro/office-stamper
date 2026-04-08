package pro.verron.officestamper.utils.openpackaging;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.relationships.Relationship;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.svg.SvgUtils;

import java.io.ByteArrayInputStream;

import static org.docx4j.openpackaging.contenttype.ContentTypes.IMAGE_SVG;
import static org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImageName;

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

    public static ImgPart newSvgPart(OpcPackage opcPackage, Part sourcePart, byte[] bytes)
            throws Docx4JException {
        var contentTypeManager = opcPackage.getContentTypeManager();
        var relationshipsPart = sourcePart.getRelationshipsPart();
        var proposedRelId = relationshipsPart.getNextId();
        var partName = createImageName(opcPackage, sourcePart, proposedRelId, "svg");
        var part = (XmlPart) contentTypeManager.newPartForContentType(IMAGE_SVG, partName, null);
        part.setRelationshipType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
        part.setContentType(new ContentType(IMAGE_SVG));
        part.setDocument(new ByteArrayInputStream(bytes));
        var relationship = sourcePart.addTargetPart(part);
        var imageInfo = SvgUtils.extractSVGImageInfo(bytes);
        return new ImgPart(imageInfo, relationship);
    }

    public static ImgPart newImgPart(WordprocessingMLPackage mlPackage, Part parts, byte[] bytes)
            throws Exception {
        var imagePart = BinaryPartAbstractImage.createImagePart(mlPackage, parts, bytes);
        var relationship = imagePart.getRelLast();
        var imageInfo = imagePart.getImageInfo();
        return new ImgPart(imageInfo, relationship);
    }

    public record ImgPart(ImageInfo imageInfo, Relationship relationship) {}
}
