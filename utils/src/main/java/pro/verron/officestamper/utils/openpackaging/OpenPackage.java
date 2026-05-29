package pro.verron.officestamper.utils.openpackaging;

import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.DefaultXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import pro.verron.officestamper.utils.UtilsException;
import pro.verron.officestamper.utils.image.ImgPart;
import pro.verron.officestamper.utils.svg.SvgUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.createImageName;
import static pro.verron.officestamper.utils.UtilsException.supply;
import static pro.verron.officestamper.utils.image.ImgUtils.detectFormat;
import static pro.verron.officestamper.utils.image.ImgUtils.supportedType;
import static pro.verron.officestamper.utils.openpackaging.OpenpackagingFactory.setupRelationship;
import static pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils.createSvgPart;

/// Represents an open package that holds a reference to an [OpcPackage] document and
/// a specific part of the package. This class provides utility methods to work with the
/// package, such as searching for image parts.
///
/// @param <T> the type of the [OpcPackage] being managed
public final class OpenPackage<T extends OpcPackage> {
    private static final Map<OpcPackage, Map<Part, OpenPackage>> pool = new ConcurrentHashMap<>();
    private final Map<Integer, Part> imgParts = new ConcurrentHashMap<>();
    private final T document;
    private final Part part;

    /// Constructs a new instance of OpenPackage with the specified document and part.
    ///
    /// @param document the document object associated with this package
    /// @param part the [Part] object representing a specific part of the document
    public OpenPackage(T document, Part part) {
        this.document = document;
        this.part = part;
        part.getPackage()
            .getParts()
            .getParts()
            .values()
            .forEach(this::hash);
    }

    private void hash(Part part) {
        switch (part) {
            case DefaultXmlPart xmlPart -> {
                var extractedXml = OpenpackagingUtils.extractXml(xmlPart);
                var hashCode = extractedXml.hashCode();
                imgParts.put(hashCode, xmlPart);
            }
            case BinaryPartAbstractImage imagePart -> {
                var extractedBytes = imagePart.getBytes();
                var hashCode = Arrays.hashCode(extractedBytes);
                imgParts.put(hashCode, imagePart);
            }
            default -> { /* DO NOTHING */ }
        }
    }

    public static <T extends OpcPackage> OpenPackage<T> getOrCreate(T document, Part part) {
        //noinspection unchecked because the pool system ensure typing is respected.
        return pool.computeIfAbsent(document, d -> new ConcurrentHashMap<>())
                   .computeIfAbsent(part, p -> new OpenPackage<>(document, p));
    }

    /// Finds an existing image part in the package that matches the given byte data, or creates a new one
    /// if no matching part is found or deduplication is disabled.
    ///
    /// @param bytes a supplier providing the byte array containing image data
    /// @param deduplicate a boolean flag indicating whether to deduplicate by checking for an existing image part
    /// @return the found or newly created `ImgPart` containing the detected image format and its relationship
    public ImgPart findOrCreateImgPart(Supplier<byte[]> bytes, boolean deduplicate) {
        if (deduplicate) {
            var foundImagePart = findImgPart(bytes.get());
            if (foundImagePart.isPresent()) return foundImagePart.get();
        }
        return newImgPart(bytes.get());
    }

    private Optional<ImgPart> findImgPart(byte[] bytes) {
        if (bytes.length == 0) throw new UtilsException("Can't create image from empty byte array");
        var format = detectFormat(bytes).orElseThrow(supply("Could not detect a supported image type."));
        var mimeType = supportedType(format.name()).orElseThrow(supply("Unsupported image type: %s", format.name()));
        ensureHasRelationshipPart();
        var relationshipId = createRelationshipId();
        var ctm = document().getContentTypeManager();
        var isSvg = mimeType.equals(ContentTypes.IMAGE_SVG);
        if (isSvg) {
            var svgXml = OpenpackagingUtils.extractSvgXml(bytes, ctm);
            var svgXmlHashcode = svgXml.hashCode();
            if (imgParts.containsKey(svgXmlHashcode)) {
                var targetPart = imgParts.get(svgXmlHashcode);
                var relationship = setupRelationship(part, targetPart, relationshipId);
                return Optional.of(new ImgPart(format, relationship));
            }
        }
        else {
            var bytesHashcode = Arrays.hashCode(bytes);
            if (imgParts.containsKey(bytesHashcode)) {
                var targetPart = imgParts.get(bytesHashcode);
                var relationship = setupRelationship(part, targetPart, relationshipId);
                return Optional.of(new ImgPart(format, relationship));
            }
        }
        return Optional.empty();
    }

    private ImgPart newImgPart(byte[] bytes) {
        if (bytes.length == 0) throw new UtilsException("Can't create image from empty byte array");

        var optFormat = detectFormat(bytes);
        var format = optFormat.orElseThrow(() -> new UtilsException("Could not detect a supported image type."));

        var optMimeType = supportedType(format.name());
        var mimeType = optMimeType.orElseThrow(() -> new UtilsException("Unsupported image type"));

        ensureHasRelationshipPart();
        var relationshipId = createRelationshipId();
        var partName = createImageName(document(), part, relationshipId, format.name());
        var ctm = document().getContentTypeManager();

        Part imgPart;
        if (mimeType.equals(ContentTypes.IMAGE_SVG)) {
            var document = SvgUtils.parseDocument(bytes);
            imgPart = createSvgPart(ctm, document, partName);
            hash(imgPart);
        }
        else {
            imgPart = OpenpackagingFactory.createImagePart(ctm, bytes, mimeType, partName);
            hash(imgPart);
        }

        var relationship = setupRelationship(part, imgPart, relationshipId);
        return new ImgPart(format, relationship);
    }

    private void ensureHasRelationshipPart() {
        if (part.getRelationshipsPart() == null) RelationshipsPart.createRelationshipsPartForPart(part);
    }

    private String createRelationshipId() {
        var relationshipsPart = part.getRelationshipsPart();
        return relationshipsPart.getNextId();
    }

    /// Retrieves the document object associated with this package.
    ///
    /// @return the document object of type T
    public T document() {return document;}

    @Override
    public String toString() {
        return "OpenPackage[document=%s, part=%s]".formatted(document, part);
    }

}
