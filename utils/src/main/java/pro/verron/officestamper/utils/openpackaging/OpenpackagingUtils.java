package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.R;
import org.w3c.dom.Document;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.image.ImageRunOptions;
import pro.verron.officestamper.utils.svg.SvgUtils;
import pro.verron.officestamper.utils.wml.WmlFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

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

    /// Creates a new run element containing an image, which is embedded in a [WordprocessingMLPackage] document.
    ///
    /// @param openPackage       the open package of the Word document, providing access to the document model
    ///                          and its sections; typically used to generate the appropriate relationships
    ///                          and content.
    /// @param bytes             a supplier of the byte array representing the image data to be embedded
    ///                          in the document.
    /// @param imageRunOptions   options for the image run, including alternate text, filename hints,
    ///                          maximum width, and deduplication preferences.
    /// @return a [R] (run) object representing the created image run, which can be added to the
    ///         document content.
    /// @throws UtilsException   if there is an error during the creation of the image part, such as
    ///                          an invalid image format or issues with the document model.
    public static R newImageRun(
            OpenPackage<WordprocessingMLPackage> openPackage,
            Supplier<byte[]> bytes,
            ImageRunOptions imageRunOptions
    ) {
        try {
            var document = openPackage.document();
            var documentModel = document.getDocumentModel();
            var sections = documentModel.getSections();
            var lastSection = sections.getLast();
            var pageDimension = lastSection.getPageDimensions();
            var imgPart = openPackage.findOrCreateImgPart(bytes, imageRunOptions.deduplicate());
            var relationship = imgPart.relationship();
            var imgFormat = imgPart.format();
            var dimension = imgFormat.dimension();
            var format = imgFormat.name();
            var scale = WmlFactory.computeScale(pageDimension,
                    imageRunOptions.maxWidth() == null ? -1 : imageRunOptions.maxWidth(),
                    dimension);
            var inline = format.equals("svg")
                    ? WmlFactory.newSVGInline(relationship,
                    imageRunOptions.filenameHint(),
                    imageRunOptions.altText(),
                    scale)
                    : WmlFactory.newImgInline(relationship,
                            imageRunOptions.filenameHint(),
                            imageRunOptions.altText(),
                            scale);
            var drawing = WmlFactory.newDrawing(inline);
            return WmlFactory.newRun(drawing);
        } catch (Exception e) {
            throw new UtilsException("Failed to create an ImagePart", e);
        }
    }

    /// Extracts the SVG XML content from the provided byte array by parsing it into a document and
    /// creating an SVG part using the specified ContentTypeManager.
    ///
    /// @param bytes the byte array containing the SVG content to be parsed
    /// @param ctm the content type manager used to manage the creation of the SVG part
    /// @return the extracted XML content as a String
    /// @throws RuntimeException if an error occurs during the processing or extraction of the XML content
    public static String extractSvgXml(byte[] bytes, ContentTypeManager ctm) {
        var newDocument = SvgUtils.parseDocument(bytes);
        var svgPart = createSvgPart(ctm, newDocument, "/temporary");
        return extractXml(svgPart);
    }

    /// Creates a new SVG part with the specified document, content type manager, and part name.
    ///
    /// @param contentTypeManager the content type manager used to create the SVG part
    /// @param document the XML document to associate with the new SVG part
    /// @param partName the name to assign to the new SVG part
    ///
    /// @return the created SVG part
    ///
    /// @throws UtilsException if an error occurs during part creation or initialization
    public static XmlPart createSvgPart(ContentTypeManager contentTypeManager, Document document, String partName) {
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

    /// Extracts the XML content from the specified XmlPart.
    ///
    /// @param xmlPart the XmlPart from which to extract the XML content
    /// @return the extracted XML content as a String
    /// @throws RuntimeException if an error occurs while retrieving the XML content
    public static String extractXml(XmlPart xmlPart) {
        String existingXml;
        try {
            existingXml = xmlPart.getXML();
        } catch (Docx4JException e) {
            throw new RuntimeException(e);
        }
        return existingXml;
    }
}
