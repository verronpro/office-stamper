package pro.verron.officestamper.preset;

import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.R;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.utils.WmlFactory;

import java.io.IOException;
import java.io.InputStream;

/// This class describes an image, which will be inserted into a document.
///
/// @author Joseph Verron
/// @author Romster
/// @version ${version}
/// @since 1.0.0
public final class Image {

    private final byte[] imageBytes;
    private final @Nullable Integer maxWidth;
    private final String filenameHint;
    private final String altText;

    /// Constructor for Image.
    ///
    /// @param in - content of the image as InputStream
    ///
    /// @throws IOException if any.
    public Image(InputStream in)
            throws IOException {
        this(in.readAllBytes());
    }

    /// Constructor for Image.
    ///
    /// @param imageBytes - content of the image as an array of the bytes
    public Image(byte[] imageBytes) {
        this(imageBytes, null);
    }

    /// Constructor for Image.
    ///
    /// @param imageBytes - content of the image as an array of the bytes
    /// @param maxWidth   - max width of the image in twip
    public Image(byte[] imageBytes, @Nullable Integer maxWidth) {
        this(imageBytes, maxWidth, "dummyFileName", "dummyAltText");
    }

    public Image(byte[] imageBytes, @Nullable Integer maxWidth, String filenameHint, String altText) {
        this.imageBytes = imageBytes;
        this.maxWidth = maxWidth;
        this.filenameHint = filenameHint;
        this.altText = altText;
    }

    /// Constructor for Image.
    ///
    /// @param in       - content of the image as InputStream
    /// @param maxWidth - max width of the image in twip
    ///
    /// @throws IOException if any.
    public Image(InputStream in, Integer maxWidth)
            throws IOException {
        this(in.readAllBytes(), maxWidth);
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
            var image = BinaryPartAbstractImage.createImagePart(mlPackage, part.part(), imageBytes);
            return WmlFactory.newRun(maxWidth, image, filenameHint, altText);
        } catch (Exception e) {
            throw new OfficeStamperException("Failed to create an ImagePart", e);
        }
    }
}
