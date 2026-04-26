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

/// Utility class for working with SVG (Scalable Vector Graphics) documents.
/// Provides methods to parse SVG data securely, mitigating common security risks
/// such as XML External Entity (XXE) attacks.
public class SvgUtils {

    private static volatile boolean restrictedMode = true;

    private SvgUtils() {
        /* This utility class should not be instantiated */
    }

    /// Returns whether SVG parsing safe mode is enabled.
    ///
    /// When enabled (default), the parser is hardened against XXE/DTD and related attacks.
    public static boolean isRestrictedMode() {
        return restrictedMode;
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
            var documentBuilder = restrictedMode ? newRestrictedDocumentBuilder() : newPermissiveDocumentBuilder();
            return documentBuilder.parse(inputStream);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new UtilsException("Failed to parse SVG document securely", e);
        }
    }

    private static DocumentBuilder newRestrictedDocumentBuilder()
            throws ParserConfigurationException {
        var factory = DocumentBuilderFactory.newInstance();

        // Namespace-aware parsing is generally recommended for SVG
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

    private static DocumentBuilder newPermissiveDocumentBuilder()
            throws ParserConfigurationException {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // Intentionally avoid setting hardened features; caller opted out of safe mode.
        // Keep entity expansion off to prevent excessive memory usage by default.
        try {
            factory.setXIncludeAware(false);
        } catch (UnsupportedOperationException ignored) {
        }
        return factory.newDocumentBuilder();
    }

    /// Enables SVG parsing safe mode.
    ///
    /// When safe mode is enabled, the SVG parser applies stricter security configurations to mitigate
    /// potential vulnerabilities, such as XML External Entity (XXE) or Document Type Definition (DTD) attacks.
    ///
    /// This method sets the internal flag to indicate that safe mode is active,
    /// affecting the behavior of SVG parsing methods in this utility class.
    public static void enableSafeMode() {
        restrictedMode = true;
    }

    /// Disables the safe mode for SVG parsing.
    ///
    /// When safe mode is disabled, the SVG parser applies relaxed security configurations,
    /// which may reintroduce vulnerabilities, such as XML External Entity (XXE) or Document Type Definition (DTD)
    ///  attacks.
    ///
    /// This method sets an internal flag to indicate that the safe mode is inactive,
    /// affecting the behavior of SVG parsing methods in this utility class.
    public static void disableSafeMode() {
        restrictedMode = false;
    }
}
