package org.wickedsource.docxstamper.replace.typeresolver.image;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class describes an image which will be inserted into a document.
 *
 * @author Joseph Verron
 * @author Romster
 * @version ${version}
 * @since 1.0.0
 */
public final class Image
        extends pro.verron.docxstamper.api.Image {

    /**
     * <p>Constructor for Image.</p>
     *
     * @param in - content of the image as InputStream
     * @throws IOException if any.
     */
    public Image(InputStream in) throws IOException {
        super(in);
    }

    /**
     * <p>Constructor for Image.</p>
     *
     * @param in - content of the image as InputStream
     * @param maxWidth - max width of the image in twip
     * @throws IOException if any.
     */
    public Image(InputStream in, Integer maxWidth) throws IOException {
        super(in, maxWidth);
    }

    /**
     * <p>Constructor for Image.</p>
     *
     * @param imageBytes - content of the image as array of the bytes
     */
    public Image(byte[] imageBytes) {
        super(imageBytes);
    }

    /**
     * <p>Constructor for Image.</p>
     *
     * @param imageBytes - content of the image as array of the bytes
     * @param maxWidth - max width of the image in twip
     */
    public Image(byte[] imageBytes, Integer maxWidth) {
        super(imageBytes, maxWidth);
    }

}
