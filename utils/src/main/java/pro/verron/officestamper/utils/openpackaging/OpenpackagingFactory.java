package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.image.ImgFormat;
import pro.verron.officestamper.utils.image.ImgPart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

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

    public static ImgPart newImgPart(OpcPackage opcPackage, Part sourcePart, byte[] bytes) {
        if (bytes.length == 0) throw new UtilsException("Can't create image from empty byte array");

        var optFormat = detectImageFormat(bytes);
        var format = optFormat.orElseThrow(() -> new UtilsException("Could not detect a supported image type."));

        var optMimeType = supportedContentType(format.name());
        var mimeType = optMimeType.orElseThrow(() -> new UtilsException("Unsupported image type"));

        ensureHasRelationshipPart(sourcePart);
        var relationshipId = createRelationshipId(sourcePart);
        var partName = createImageName(opcPackage, sourcePart, relationshipId, format.name());
        var ctm = opcPackage.getContentTypeManager();

        var relationship = mimeType.equals(ContentTypes.IMAGE_SVG)
                ? createSvgPart(sourcePart, bytes, ctm, partName)
                : createImagePart(sourcePart, bytes, ctm, mimeType, partName, relationshipId);


        return new ImgPart(format, relationship);
    }

    private static Optional<ImgFormat> detectImageFormat(byte[] bytes) {
        try (var imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) return Optional.empty();
            var reader = readers.next();
            reader.setInput(imageInputStream, false, false);
            var formatName = reader.getFormatName();
            var width = reader.getWidth(0);
            var height = reader.getHeight(0);
            var imgFormat = new ImgFormat(formatName, new Dimension(width, height));
            reader.dispose();
            return Optional.of(imgFormat);
        } catch (IOException e) {
            throw new UtilsException(e);
        }

    }

    private static Optional<String> supportedContentType(String imageType) {
        var supportedImageTypes = new HashMap<String, String>();
        supportedImageTypes.put("emf", ContentTypes.IMAGE_EMF);
        supportedImageTypes.put("svg", ContentTypes.IMAGE_SVG);
        supportedImageTypes.put("wmf", ContentTypes.IMAGE_WMF);
        supportedImageTypes.put("tif", ContentTypes.IMAGE_TIFF);
        supportedImageTypes.put("png", ContentTypes.IMAGE_PNG);
        supportedImageTypes.put("jpeg", ContentTypes.IMAGE_JPEG);
        supportedImageTypes.put("gif", ContentTypes.IMAGE_GIF);
        supportedImageTypes.put("bmp", ContentTypes.IMAGE_BMP);
        return Optional.ofNullable(supportedImageTypes.get(imageType.toLowerCase()));
    }

    private static void ensureHasRelationshipPart(Part sourcePart) {
        if (sourcePart.getRelationshipsPart() == null) RelationshipsPart.createRelationshipsPartForPart(sourcePart);
    }

    private static String createRelationshipId(Part sourcePart) {
        var relationshipsPart = sourcePart.getRelationshipsPart();
        return relationshipsPart.getNextId();
    }

    private static Relationship createSvgPart(
            Part sourcePart,
            byte[] bytes,
            ContentTypeManager contentTypeManager,
            String partName
    ) {
        XmlPart part;
        try {
            part = (XmlPart) contentTypeManager.newPartForContentType(ContentTypes.IMAGE_SVG, partName, null);
        } catch (InvalidFormatException | PartUnrecognisedException e) {
            throw new UtilsException(e);
        }
        part.setRelationshipType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
        part.setContentType(new ContentType(ContentTypes.IMAGE_SVG));
        try {
            part.setDocument(new ByteArrayInputStream(bytes));
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
        try {
            return sourcePart.addTargetPart(part);
        } catch (InvalidFormatException e) {
            throw new UtilsException(e);
        }
    }

    private static Relationship createImagePart(
            Part sourcePart,
            byte[] bytes,
            ContentTypeManager ctm,
            String mimeType,
            String partName,
            String relationshipId
    ) {
        try {
            var imagePart = (BinaryPartAbstractImage) ctm.newPartForContentType(mimeType, partName, null);
            imagePart.setBinaryData(new ByteArrayInputStream(bytes));
            return setupRelationship(sourcePart, imagePart, relationshipId);
        } catch (InvalidFormatException | PartUnrecognisedException e) {
            throw new UtilsException(e);
        }
    }

    private static Relationship setupRelationship(
            Part sourcePart,
            BinaryPartAbstractImage targetPart,
            String relationshipId
    ) {
        try {
            Relationship relationship = sourcePart.addTargetPart(targetPart, relationshipId);
            var relationships = targetPart.getRels();
            relationships.add(relationship);
            return relationship;
        } catch (InvalidFormatException e) {
            throw new UtilsException(e);
        }
    }
}
