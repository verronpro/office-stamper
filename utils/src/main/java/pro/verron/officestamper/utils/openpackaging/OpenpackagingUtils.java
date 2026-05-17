package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.exceptions.PartUnrecognisedException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.XmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.R;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Document;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.image.ImageRunOptions;
import pro.verron.officestamper.utils.image.ImgPart;
import pro.verron.officestamper.utils.svg.SvgUtils;
import pro.verron.officestamper.utils.wml.WmlFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static pro.verron.officestamper.utils.openpackaging.OpenpackagingFactory.newImgPart;
import static pro.verron.officestamper.utils.image.ImgUtils.detectFormat;
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

    /// Creates a new run containing an image from the given WordprocessingMLPackage and Part. This method processes the
    /// input document to locate or create an image part, establishes the necessary relationships, and generates a run
    /// element with the image embedded.
    ///
    /// @param document the WordprocessingMLPackage containing the document where the image will be added
    /// @param part the source Part in the document package that relates to the image
    ///
    /// @return a new run object containing the embedded image
    ///
    /// @throws UtilsException if the creation of the image part or the run fails
    public static R newImageRun(
            WordprocessingMLPackage document,
            Part part,
            Supplier<byte[]> bytes,
            ImageRunOptions imageRunOptions
    ) {
        try {
            var documentModel = document.getDocumentModel();
            var sections = documentModel.getSections();
            var lastSection = sections.getLast();
            var pageDimension = lastSection.getPageDimensions();
            var imgPart = findOrCreate(document, part, bytes, imageRunOptions);
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

    private static @NonNull ImgPart findOrCreate(
            WordprocessingMLPackage document,
            Part part,
            Supplier<byte[]> bytes,
            ImageRunOptions imageRunOptions
    ) {
        if (!imageRunOptions.deduplicate()) return newImgPart(document, part, bytes.get());

        var foundImagePart = findImgPart(document, part, bytes.get());
        return foundImagePart.orElseGet(() -> newImgPart(document, part, bytes.get()));
    }

    public static Optional<ImgPart> findImgPart(OpcPackage opcPackage, Part sourcePart, byte[] bytes) {
    /// Creates a new run containing an image from the given WordprocessingMLPackage and Part. This method processes the
    /// input document to locate or create an image part, establishes the necessary relationships, and generates a run
    /// element with the image embedded.
    ///
    /// @param document the WordprocessingMLPackage containing the document where the image will be added
    ///
    /// @return a new run object containing the embedded image
    ///
    /// @throws UtilsException if the creation of the image part or the run fails
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
            var imgPart = findOrCreate(openPackage, bytes);
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

    private static ImgPart findOrCreate(
            OpenPackage<?> openPackage,
            Supplier<byte[]> imageBytesSupplier
    ) {
        var foundImagePart = findImgPart(openPackage, imageBytesSupplier.get());
        return foundImagePart.orElseGet(() -> newImgPart(openPackage, imageBytesSupplier.get()));
    }

    /// Searches for an existing image part in the provided package that matches the given byte data.
    /// If a matching image part is found, an `ImgPart` object representing the image part and its
    /// relationship is returned.
    ///
    /// @param openPackage the package containing the document where the image part might exist
    /// @param bytes the byte array representing the image data to be matched
    ///
    /// @return an `Optional` containing the matching `ImgPart`, or `Optional.empty()`
    ///         if no matching image part is found
    ///
    /// @throws UtilsException if the byte array is empty or the image format cannot be detected
    public static Optional<ImgPart> findImgPart(OpenPackage<?> openPackage, byte[] bytes) {
        if (bytes.length == 0) throw new UtilsException("Can't create image from empty byte array");

        var format = detectFormat(bytes).orElseThrow(supply("Could not detect a supported image type."));
        var mimeType = supportedType(format.name()).orElseThrow(supply("Unsupported image type: %s", format.name()));
        ensureHasRelationshipPart(openPackage.part());
        var relationshipId = createRelationshipId(openPackage.part());
        var ctm = openPackage.document()
                             .getContentTypeManager();
        var isSvg = mimeType.equals(ContentTypes.IMAGE_SVG);
        var subParts = openPackage.part()
                                  .getPackage()
                                  .getParts()
                                  .getParts();
        for (var subPart : subParts.values()) {
            switch (subPart) {
                case DefaultXmlPart xmlPart when isSvg -> {
                    var existingXml = extractXml(xmlPart);
                    var newXml = extractXml(bytes, ctm);
                    if (Objects.equals(newXml, existingXml)) {
                        var relationship = setupRelationship(openPackage.part(), xmlPart, relationshipId);
                        return Optional.of(new ImgPart(format, relationship));
                    }
                }
                case BinaryPartAbstractImage imagePart -> {
                    if (Arrays.equals(imagePart.getBytes(), bytes)) {
                        var relationship = setupRelationship(openPackage.part(), imagePart, relationshipId);
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

    static XmlPart createSvgPart(Document document, ContentTypeManager contentTypeManager, String partName) {
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
