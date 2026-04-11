package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.svg.SvgUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.util.Set;

import static org.docx4j.openpackaging.contenttype.ContentTypes.IMAGE_SVG;
import static org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImageName;

/// Utility class for creating Open Packaging objects.
///
/// This class provides helper methods to create instances of docx4j Open Packaging objects, wrapping checked exceptions
/// in runtime [UtilsException] for easier handling.
public class OpenpackagingFactory {

    private static final Logger log = LoggerFactory.getLogger(OpenpackagingFactory.class);

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
        var imageDimensions = SvgUtils.extractSVGImageInfo(bytes);
        return new ImgPart(imageDimensions, relationship);
    }

    public static ImgPart newImgPart(OpcPackage opcPackage, Part sourcePart, byte[] bytes)
            throws Exception {
        if (bytes.length == 0) throw new UtilsException("Can't create image from empty byte array");

        var imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        var imageReaders = ImageIO.getImageReaders(imageInputStream);
        String formatName = null;
        int width = 0;
        int height = 0;
        while (imageReaders.hasNext()) {
            var imageReader = imageReaders.next();
            imageReader.setInput(imageInputStream);
            formatName = imageReader.getFormatName();
            var image = imageReader.read(0);
            width = image.getWidth();
            height = image.getHeight();
        }
        if (formatName == null) throw new UtilsException("Did not find a reader for that image");
        var supportedImageTypes = Set.of("tiff", "emf", "wmf", "png", "jpeg", "gif", "bmp");
        if (!supportedImageTypes.contains(formatName.toLowerCase()))
            throw new UtilsException("Unsupported linked image type: " + formatName);
        var ctm = opcPackage.getContentTypeManager();
        if (sourcePart.getRelationshipsPart() == null) RelationshipsPart.createRelationshipsPartForPart(sourcePart);
        var relationshipsPart = sourcePart.getRelationshipsPart();
        var proposedRelId = relationshipsPart.getNextId();
        var partName = createImageName(opcPackage, sourcePart, proposedRelId, formatName);
        var contentType = "image/%s".formatted(formatName.toLowerCase());
        var imagePart = (BinaryPartAbstractImage) ctm.newPartForContentType(contentType, partName, null);
        imagePart.setBinaryData(new ByteArrayInputStream(bytes));
        var relationship = sourcePart.addTargetPart(imagePart, proposedRelId);
        var relationships = imagePart.getRels();
        relationships.add(relationship);
        Dimension2D imageDimensions = new Dimension(width, height);
        return new ImgPart(imageDimensions, relationship);
    }


    public record ImgPart(Dimension2D dimension, Relationship relationship) {}
}
