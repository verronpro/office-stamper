package pro.verron.officestamper.utils.svg;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import pro.verron.officestamper.utils.UtilsException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SvgUtils {

    private SvgUtils() {
        /* This utility class should not be instantiated */
    }

    /// Parse an SVG XML document from bytes with hardened XML parser settings.
    ///
    /// - Disables DTDs and external entity resolution to prevent XXE attacks
    /// - Enables secure processing
    /// - Disables XInclude and entity expansion
    ///
    /// @param bytes the SVG content as a UTF-8 encoded byte array
    /// @return the parsed DOM Document
    /// @throws UtilsException if parsing fails or the parser cannot be securely configured
    public static Document parseDocument(byte[] bytes) {
        var inputStream = new ByteArrayInputStream(bytes);
        try {
            var documentBuilder = newSecureDocumentBuilder();
            return documentBuilder.parse(inputStream);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new UtilsException("Failed to parse SVG document securely", e);
        }
    }

    private static DocumentBuilder newSecureDocumentBuilder()
            throws ParserConfigurationException {
        var factory = DocumentBuilderFactory.newInstance();

        // Namespace aware parsing is generally recommended for SVG
        factory.setNamespaceAware(true);

        // Harden against XXE/DTD
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // Disallow any DOCTYPE
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // Prevent external entity resolution
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        // XInclude and entity expansion
        try {
            factory.setXIncludeAware(false);
        } catch (UnsupportedOperationException ignored) {
            // Some implementations may not support XInclude; safe to ignore
        }
        factory.setExpandEntityReferences(false);

        return factory.newDocumentBuilder();
    }
}
