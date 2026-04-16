package pro.verron.officestamper.imageio.wmf;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Iterator;

/// Minimal ImageIO reader for Placeable WMF that exposes only image dimensions.
///
/// Implementation parses the 22-byte Placeable WMF header to get the bounding box in metafile units
/// and the `Inch` units-per-inch factor. Dimensions are converted to pixels using an estimated
/// DPI (96 by default). Rasterization is not supported and [#read(int, ImageReadParam)] throws.
public final class WmfImageReader
        extends ImageReader {

    private static final int PLACEABLE_KEY = 0x9AC6CDD7; // little-endian in file: D7 CD C6 9A

    private Dimension cachedSize;

    WmfImageReader(WmfImageReaderSpi spi) {
        super(spi);
    }

    @Override
    public String getFormatName() {
        return "wmf";
    }

    @Override
    public int getNumImages(boolean allowSearch)
            throws IIOException {
        ensureInputSet();
        return 1;
    }

    private void ensureInputSet()
            throws IIOException {
        if (!(getInput() instanceof ImageInputStream)) {
            throw new IIOException("Input must be an ImageInputStream");
        }
    }

    @Override
    public int getWidth(int imageIndex)
            throws IIOException {
        checkImageIndex(imageIndex);
        return getOrParseSize().width;
    }

    private void checkImageIndex(int imageIndex)
            throws IIOException {
        if (imageIndex != 0) throw new IIOException("WMF reader supports a single image (index 0)");
        ensureInputSet();
    }

    private Dimension getOrParseSize()
            throws IIOException {
        if (cachedSize != null) return cachedSize;
        var iis = (ImageInputStream) getInput();
        long pos = 0L;
        try {
            pos = iis.getStreamPosition();
            var oldOrder = iis.getByteOrder();
            iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            // Placeable WMF header (22 bytes)
            int key = iis.readInt();
            if (key != PLACEABLE_KEY) {
                throw new IIOException("Not a Placeable WMF (missing magic key)");
            }
            /* hmf */
            iis.readUnsignedShort();

            // Bounding box in metafile units (signed 16-bit shorts)
            short left = iis.readShort();
            short top = iis.readShort();
            short right = iis.readShort();
            short bottom = iis.readShort();

            int inch = iis.readUnsignedShort(); // units per inch
            /* reserved */
            iis.readInt();
            /* checksum */
            iis.readUnsignedShort();

            // Restore order ASAP
            iis.setByteOrder(oldOrder);
            iis.seek(pos); // rewind for other readers if needed

            int unitsW = (right - left);
            int unitsH = (bottom - top);
            if (unitsW <= 0 || unitsH <= 0) {
                throw new IIOException("Invalid WMF bounding box");
            }

            double dpiX = 96.0;
            double dpiY = 96.0;
            double unitsPerInch = (inch > 0) ? inch : 1440.0; // common default if missing

            int width = (int) Math.round((unitsW / unitsPerInch) * dpiX);
            int height = (int) Math.round((unitsH / unitsPerInch) * dpiY);

            if (width <= 0 || height <= 0) {
                throw new IIOException("Could not determine WMF image dimensions");
            }
            cachedSize = new Dimension(width, height);
        } catch (IOException e) {
            try {iis.seek(pos);} catch (IOException ignore) {}
            throw new IIOException("Failed to read WMF header", e);
        }
        return cachedSize;
    }

    @Override
    public int getHeight(int imageIndex)
            throws IIOException {
        checkImageIndex(imageIndex);
        return getOrParseSize().height;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IIOException {
        checkImageIndex(imageIndex);
        return Collections.emptyIterator();
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    @Override
    public IIOMetadata getStreamMetadata() {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex)
            throws IIOException {
        checkImageIndex(imageIndex);
        return null;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) {
        throw new UnsupportedOperationException("WMF rasterization is not supported by this reader");
    }
}
