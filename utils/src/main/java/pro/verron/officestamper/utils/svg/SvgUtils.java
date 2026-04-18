package pro.verron.officestamper.utils.svg;

import org.docx4j.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pro.verron.officestamper.utils.UtilsException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SvgUtils {

    private SvgUtils() {
        /* This utility class should not be instantiated */
    }

    public static Document parseDocument(byte[] bytes) {
        var documentBuilder = XmlUtils.getNewDocumentBuilder();
        var inputStream = new ByteArrayInputStream(bytes);
        try {
            return documentBuilder.parse(inputStream);
        } catch (SAXException | IOException e) {
            throw new UtilsException(e);
        }
    }
}
