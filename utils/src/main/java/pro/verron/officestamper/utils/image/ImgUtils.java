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

public class ImgUtils {
    private static final Logger log = LoggerFactory.getLogger(ImgUtils.class);

    public static Optional<ImgFormat> detectFormat(byte[] bytes) {
        var inputStream = new ByteArrayInputStream(bytes);
        try (var imageInputStream = ImageIO.createImageInputStream(inputStream)) {
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
            var imgFormat = new ImgFormat(formatName, new Dimension(width, height));
            reader.dispose();
            return Optional.of(imgFormat);
        } catch (IOException e) {
            throw new UtilsException(e);
        }

    }

    public static Optional<String> supportedContentType(String imageType) {
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
}
