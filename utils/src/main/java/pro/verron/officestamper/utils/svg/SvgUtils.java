package pro.verron.officestamper.utils.svg;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.util.MimeConstants;
import org.docx4j.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pro.verron.officestamper.utils.UtilsException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SvgUtils {
    private static final Logger log = LoggerFactory.getLogger(SvgUtils.class);

    private SvgUtils() {
        /* This utility class should not be instantiated */
    }

    public static boolean isSvg(byte[] bytes) {
        if (bytes.length < 4) return false;
        String content = new String(bytes, 0, Math.min(bytes.length, 1000), UTF_8);
        return content.contains("<svg") || content.contains("http://www.w3.org/2000/svg");
    }

    public static ImageInfo extractSVGImageInfo(byte[] bytes) {
        var documentBuilder = XmlUtils.getNewDocumentBuilder();
        Document doc;
        try {
            doc = documentBuilder.parse(new ByteArrayInputStream(bytes));
        } catch (SAXException | IOException e) {
            throw new UtilsException("An error occurred while parsing a svg", e);
        }
        var svgRoot = doc.getDocumentElement();

        var width = -1.0;
        double height = -1.0;

        // Try width/height attributes first
        if (svgRoot.hasAttribute("width") && svgRoot.hasAttribute("height")) {
            width = parseLength(svgRoot.getAttribute("width"));
            height = parseLength(svgRoot.getAttribute("height"));
        }

        // If missing, try viewBox
        if ((width <= 0 || height <= 0) && svgRoot.hasAttribute("viewBox")) {
            var viewBox = svgRoot.getAttribute("viewBox");
            var trimmedViewBox = viewBox.trim();
            String[] vb = trimmedViewBox.split("\\s+");
            if (vb.length == 4) {
                width = Double.parseDouble(vb[2]);
                height = Double.parseDouble(vb[3]);
            }
        }

        var imageSize = new ImageSize((int) width, (int) height, 72);
        var imageInfo = new ImageInfo("whatever", MimeConstants.MIME_SVG);
        imageInfo.setSize(imageSize);
        return imageInfo;
    }

    private static double parseLength(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.\\-]", ""));
        } catch (NumberFormatException e) {
            log.info("An error occurred while parsing a svg length", e);
            return -1;
        }
    }
}
