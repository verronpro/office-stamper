package pro.verron.officestamper.utils.openpackaging;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageContext;
import org.apache.xmlgraphics.image.loader.impl.DefaultImageSessionContext;
import org.apache.xmlgraphics.image.loader.spi.ImagePreloader;
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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.svg.SvgUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Set;

import static org.docx4j.openpackaging.contenttype.ContentTypes.*;
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
        var imageInfo = SvgUtils.extractSVGImageInfo(bytes);
        return new ImgPart(imageInfo, relationship);
    }

    public static ImgPart newImgPart(OpcPackage opcPackage, Part sourcePart, byte[] bytes)
            throws Exception {
        if (bytes.length == 0) throw new Docx4JException("Can't create image from empty byte array");
        var tmpImageFile = File.createTempFile("img", ".img");
        var fos = new FileOutputStream(tmpImageFile);
        fos.write(bytes);
        fos.close();
        var uri = tmpImageFile.toURI();
        var url = uri.toURL();
        var info = getSupportedImageInfo(url);

        var ctm = opcPackage.getContentTypeManager();
        if (sourcePart.getRelationshipsPart() == null) RelationshipsPart.createRelationshipsPartForPart(sourcePart);

        var relationshipsPart = sourcePart.getRelationshipsPart();
        var proposedRelId = relationshipsPart.getNextId();

        var mimeType = info.getMimeType();
        var ext = mimeType.substring(mimeType.indexOf("/") + 1);

        var partName = createImageName(opcPackage, sourcePart, proposedRelId, ext);
        var imagePart = (BinaryPartAbstractImage) ctm.newPartForContentType(mimeType, partName, null);

        log.debug("created part {} with name {}",
                imagePart.getClass()
                         .getName(),
                imagePart.getPartName()
                         .toString());

        imagePart.setBinaryData(new ByteArrayInputStream(bytes));

        var relationship = sourcePart.addTargetPart(imagePart, proposedRelId);
        imagePart.getRels()
                 .add(relationship);

        return new ImgPart(info, relationship);
    }

    private static @NonNull ImageInfo getSupportedImageInfo(URL url) {
        ImageInfo info;
        try {
            info = getImageInfo(url);
            var supportedImageTypes = Set.of(IMAGE_TIFF,
                    IMAGE_EMF2,
                    IMAGE_WMF,
                    IMAGE_PNG,
                    IMAGE_JPEG,
                    IMAGE_GIF,
                    IMAGE_BMP);
            if (!supportedImageTypes.contains(info.getMimeType()))
                throw new UtilsException("Unsupported linked image type.");
        } catch (Exception e) {
            throw new UtilsException("Error checking image format", e);
        }
        return info;
    }

    private static ImageInfo getImageInfo(URL url)
            throws Docx4JException {
        var imageManager = new ImageManager(new DefaultImageContext());
        var imageContext = imageManager.getImageContext();
        try {
            var sessionContext = new DefaultImageSessionContext(imageContext, null);
            var source = sessionContext.needSource(url.toString());
            var registry = imageManager.getRegistry();
            var preloaderIterator = registry.getPreloaderIterator();
            ImageInfo info;
            while (preloaderIterator.hasNext()) {
                var p = (ImagePreloader) preloaderIterator.next();
                info = p.preloadImage(url.toString(), source, imageContext);
                if (info != null) return info;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            log.info(e.getMessage(), e);
        }
        throw new Docx4JException("Unsupported linked image type.");
    }

    public record ImgPart(ImageInfo imageInfo, Relationship relationship) {}
}
