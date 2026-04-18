package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.w3c.dom.Document;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.image.ImgPart;
import pro.verron.officestamper.utils.image.ImgUtils;
import pro.verron.officestamper.utils.svg.SvgUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static pro.verron.officestamper.utils.openpackaging.OpenpackagingFactory.setupRelationship;

/// Utility class for working with Open Packaging documents. This class provides methods to load and export Word
/// documents using DOCX4J
public class OpenpackagingUtils {
    private OpenpackagingUtils() {
        throw new UtilsException("Utility class shouldn't be instantiated");
    }

    /// Loads a Word document from the provided input stream.
    ///
    /// @param is the input stream containing the Word document data
    ///
    /// @return a WordprocessingMLPackage representing the loaded document
    ///
    /// @throws UtilsException if there is an error loading the document
    public static WordprocessingMLPackage loadWord(InputStream is) {
        try {
            return WordprocessingMLPackage.load(is);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }

    /// Exports a Word document to the provided output stream.
    ///
    /// @param wordprocessingMLPackage the Word document to export
    /// @param os the output stream to write the document to
    ///
    /// @throws UtilsException if there is an error exporting the document
    public static void exportWord(WordprocessingMLPackage wordprocessingMLPackage, OutputStream os) {
        try {
            wordprocessingMLPackage.save(os);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }


    /// Loads a PowerPoint document from the provided input stream.
    ///
    /// @param is the input stream containing the PowerPoint document data
    ///
    /// @return a PresentationMLPackage representing the loaded document
    ///
    /// @throws UtilsException if there is an error loading the document
    public static PresentationMLPackage loadPowerPoint(InputStream is) {
        try {
            return PresentationMLPackage.load(is);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }


    /// Exports a PowerPoint document to the provided output stream.
    ///
    /// @param presentationMLPackage the PowerPoint document to export
    /// @param os the output stream to write the document to
    ///
    /// @throws UtilsException if there is an error exporting the document
    public static void exportPowerPoint(PresentationMLPackage presentationMLPackage, OutputStream os) {
        try {
            presentationMLPackage.save(os);
        } catch (Docx4JException e) {
            throw new UtilsException(e);
        }
    }

    public static Optional<ImgPart> findImgPart(OpcPackage opcPackage, Part sourcePart, byte[] bytes) {
        if (bytes.length == 0) throw new UtilsException("Can't create image from empty byte array");

        var format = ImgUtils.detectFormat(bytes)
                             .orElseThrow(() -> new UtilsException("Could not detect a supported image type."));

        var mimeType = ImgUtils.supportedContentType(format.name())
                               .orElseThrow(() -> new UtilsException("Unsupported image " + "type"));

        ensureHasRelationshipPart(sourcePart);
        var relationshipId = createRelationshipId(sourcePart);
        var ctm = opcPackage.getContentTypeManager();
        var isSvg = mimeType.equals(ContentTypes.IMAGE_SVG);
        var parts = sourcePart.getPackage()
                              .getParts()
                              .getParts();
        for (var part : parts.values()) {
            switch (part) {
                case DefaultXmlPart xmlPart when isSvg -> {
                    var existingXml = extractXml(xmlPart);
                    var newXml = extractXml(bytes, ctm);
                    if (Objects.equals(newXml, existingXml)) {
                        var relationship = setupRelationship(sourcePart, xmlPart, relationshipId);
                        return Optional.of(new ImgPart(format, relationship));
                    }
                }
                case BinaryPartAbstractImage imagePart -> {
                    if (Arrays.equals(imagePart.getBytes(), bytes)) {
                        var relationship = setupRelationship(sourcePart, imagePart, relationshipId);
                        return Optional.of(new ImgPart(format, relationship));
                    }
                }
                case null, default -> { /* DO NOTHING */ }
            }
        }
        return Optional.empty();
    }

    static void ensureHasRelationshipPart(Part sourcePart) {
        if (sourcePart.getRelationshipsPart() == null) RelationshipsPart.createRelationshipsPartForPart(sourcePart);
    }

    static String createRelationshipId(Part sourcePart) {
        var relationshipsPart = sourcePart.getRelationshipsPart();
        return relationshipsPart.getNextId();
    }

    private static String extractXml(XmlPart xmlPart) {
        String existingXml;
        try {
            existingXml = xmlPart.getXML();
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        return existingXml;
    }

    private static String extractXml(byte[] bytes, ContentTypeManager ctm) {
        var newDocument = SvgUtils.parseDocument(bytes);
        var svgPart = createSvgPart(newDocument, ctm, "/temporary");
        return extractXml(svgPart);
    }

    static XmlPart createSvgPart(
            Document document,
            ContentTypeManager contentTypeManager,
            String partName
    ) {
        XmlPart part;
        try {
            part = (XmlPart) contentTypeManager.newPartForContentType(ContentTypes.IMAGE_SVG, partName, null);
        } catch (InvalidFormatException | PartUnrecognisedException e) {
            throw new UtilsException(e);
        }
        part.setRelationshipType(Namespaces.IMAGE);
        part.setContentType(new ContentType(ContentTypes.IMAGE_SVG));
        part.setDocument(document);
        return part;
    }
}
