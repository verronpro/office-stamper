package pro.verron.officestamper.preset;

import org.docx4j.wml.R;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.utils.wml.WmlFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static pro.verron.officestamper.utils.openpackaging.OpenpackagingFactory.newImgPart;
import static pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils.findImgPart;

/// This class describes an image, which will be inserted into a document.
///
/// @author Joseph Verron
/// @author Romster
/// @version ${version}
/// @since 1.0.0
public final class Image {
    private final InputStream source;
    private final @Nullable Integer maxWidth;
    private final String filenameHint;
    private final String altText;
    private byte @Nullable [] bytes = null;

    /// Constructor for Image.
    ///
    /// @param source - content of the image as InputStream
    ///
    /// @throws IOException if any.
    public Image(InputStream source)
            throws IOException {
        this(source, null);
    }

    /// Constructor for Image.
    ///
    /// @param source content of the image as InputStream
    /// @param maxWidth max width of the image in twip
    public Image(InputStream source, @Nullable Integer maxWidth) {
        this(source, maxWidth, "dummyFileName", "dummyAltText");
    }

    /// Constructor for Image.
    ///
    /// @param source content of the image as InputStream
    /// @param maxWidth max width of the image in twip
    /// @param filenameHint filename hint for the image.
    /// @param altText alternative text for the image.
    public Image(InputStream source, @Nullable Integer maxWidth, String filenameHint, String altText) {
        this.source = source;
        this.maxWidth = maxWidth;
        this.filenameHint = filenameHint;
        this.altText = altText;
    }

    /// Constructor for Image.
    ///
    /// @param imageBytes - content of the image as an array of the bytes
    public Image(byte[] imageBytes) {
        this(new ByteArrayInputStream(imageBytes), null);
    }

    /// Constructor for Image.
    ///
    /// @param imageBytes - content of the image as an array of the bytes
    /// @param maxWidth   - max width of the image in twip
    public Image(byte[] imageBytes, @Nullable Integer maxWidth) {
        this(imageBytes, maxWidth, "dummyFileName", "dummyAltText");
    }

    /// Constructor for Image.
    ///
    /// @param imageBytes content of the image as an array of the bytes
    /// @param maxWidth max width of the image in twip
    /// @param filenameHint filename hint for the image.
    /// @param altText alternative text for the image.
    public Image(byte[] imageBytes, @Nullable Integer maxWidth, String filenameHint, String altText) {
        var inputStream = new ByteArrayInputStream(imageBytes);
        this(inputStream, maxWidth, filenameHint, altText);
    }

    /// Creates a new run with the provided image and associated metadata.
    ///
    /// TODO: adding the same image twice will put the image twice into the docx-zip file.
    ///  We should make the second addition of the same image a reference instead.
    ///
    /// @param part The document part where the image will be inserted.
    ///
    /// @return The created run containing the image.
    ///
    /// @throws OfficeStamperException If there is an error creating the image part
    public R newRun(DocxPart part) {
        var mlPackage = part.document();
        try {
            var parts = part.part();
            var document = part.document();
            var documentModel = document.getDocumentModel();
            var sections = documentModel.getSections();
            var lastSection = sections.getLast();
            var pageDimension = lastSection.getPageDimensions();
            var imgPart = findImgPart(mlPackage, parts, bytes()).orElseGet(() -> newImgPart(mlPackage, parts, bytes()));
            var relationship = imgPart.relationship();
            var imgFormat = imgPart.format();
            var dimension = imgFormat.dimension();
            var format = imgFormat.name();
            var scale = WmlFactory.computeScale(pageDimension, maxWidth == null ? -1 : maxWidth, dimension);
            var inline = format.equals("svg")
                    ? WmlFactory.newSVGInline(relationship, filenameHint, altText, scale)
                    : WmlFactory.newImgInline(relationship, filenameHint, altText, scale);
            var drawing = WmlFactory.newDrawing(inline);
            return WmlFactory.newRun(drawing);
        } catch (Exception e) {
            throw new OfficeStamperException("Failed to create an ImagePart", e);
        }
    }

    private synchronized byte[] bytes() {
        if (bytes == null) try (InputStream source = this.source) {
            bytes = source.readAllBytes();
        } catch (IOException e) {
            throw new OfficeStamperException("Failed to cache the image bytes", e);
        }
        return bytes;
    }

}
