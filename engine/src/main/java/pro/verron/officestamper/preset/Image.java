package pro.verron.officestamper.preset;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.ofNullable;

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

    public synchronized byte[] getBytes() {
        if (bytes == null) try (InputStream source = this.source) {
            bytes = source.readAllBytes();
        } catch (IOException e) {
            throw new OfficeStamperException("Failed to cache the image bytes", e);
        }
        return bytes;
    }

    public String getAltText() {
        return altText;
    }

    public String getFilenameHint() {
        return filenameHint;
    }

    public Optional<Integer> getMaxWidth() {
        return ofNullable(maxWidth);
    }
}
