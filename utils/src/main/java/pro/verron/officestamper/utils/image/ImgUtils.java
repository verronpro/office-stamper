package pro.verron.officestamper.utils.image;

import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.verron.officestamper.utils.UtilsException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/// Utility class for image-related operations such as format detection and
/// content type mapping.
public class ImgUtils {

    private static final Logger log = LoggerFactory.getLogger(ImgUtils.class);

    private ImgUtils() {throw new IllegalStateException("Utility class");}

    /// Detects the format and dimensions of an image from its byte content.
    ///
    /// @param bytes the byte content of the image
    ///
    /// @return an [Optional] containing the detected [ImgFormat], or empty if
    /// no reader is found
    ///
    /// @throws UtilsException if an I/O error occurs during format detection
    public static Optional<ImgFormat> detectFormat(byte[] bytes) {
        var inputStream = new ByteArrayInputStream(bytes);
        try (
                var imageInputStream = ImageIO.createImageInputStream(
                        inputStream)
        ) {
            var readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                log.debug("Could not find an image reader for this file");
                return Optional.empty();
            }
            var reader = readers.next();
            reader.setInput(imageInputStream, false, false);
            var formatName = reader.getFormatName();
            var width = reader.getWidth(0);
            var height = reader.getHeight(0);
            var imgFormat = new ImgFormat(formatName,
                    new Dimension(width, height));
            reader.dispose();
            return Optional.of(imgFormat);
        } catch (IOException e) {
            throw new UtilsException(e);
        }

    }

    /// Returns the MIME content type for a given image type string.
    ///
    /// Supported types include: emf, svg, wmf, tif, png, jpeg, gif, bmp.
    ///
    /// @param imageType the image type string (case-insensitive)
    ///
    /// @return an [Optional] containing the MIME content type, or empty if the
    /// type is not supported
    public static Optional<String> supportedType(String imageType) {
        var supportedImageTypes = new HashMap<String, String>();
        supportedImageTypes.put("emf", ContentTypes.IMAGE_EMF);
        supportedImageTypes.put("svg", ContentTypes.IMAGE_SVG);
        supportedImageTypes.put("wmf", ContentTypes.IMAGE_WMF);
        supportedImageTypes.put("tif", ContentTypes.IMAGE_TIFF);
        supportedImageTypes.put("png", ContentTypes.IMAGE_PNG);
        supportedImageTypes.put("jpeg", ContentTypes.IMAGE_JPEG);
        supportedImageTypes.put("gif", ContentTypes.IMAGE_GIF);
        supportedImageTypes.put("bmp", ContentTypes.IMAGE_BMP);
        var normalizedType = imageType.toLowerCase();
        var mimeType = supportedImageTypes.get(normalizedType);
        return ofNullable(mimeType);
    }
}
