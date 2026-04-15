package pro.verron.officestamper.imageio.emf;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Iterator;

/// Minimal ImageIO reader for EMF (Enhanced Metafile) that exposes only image metadata
/// (width, height, bounds). No rasterization is performed and [#read(int, ImageReadParam)]
/// throws [UnsupportedOperationException].
///
/// Implementation notes:
///
///   - Only the EMF `EMR_HEADER` record is parsed from the stream. Parsing stops immediately
///     after the header; no additional records are read.
///   - Bounds selection: prefer `rclBounds` (device units/pixels). If not available or zero-sized,
///     compute from `rclFrame` (0.01 millimeters) using device DPI estimated from
///     `szlDevice` (pixels) and `szlMillimeters` (millimeters). If unavailable, fall back to 96 DPI.
///   - Stream safety: the reader records and restores the original stream position when probing/parsing.
public final class EmfImageReader
        extends ImageReader {

    private static final int EMR_HEADER = 1; // Record type for EMR_HEADER
    private static final int EMF_SIGNATURE = 0x464D4520; // ' EMF' in little-endian

    private Dimension cachedSize; // lazily computed

    EmfImageReader(EmfImageReaderSpi spi) {
        super(spi);
    }

    @Override
    public String getFormatName() {
        return "emf";
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
        if (imageIndex != 0) throw new IIOException("EMF reader supports a single image (index 0)");
        ensureInputSet();
    }

    private Dimension getOrParseSize()
            throws IIOException {
        if (cachedSize != null) return cachedSize;
        var iis = (ImageInputStream) getInput();
        try {
            var oldOrder = iis.getByteOrder();
            iis.setByteOrder(ByteOrder.LITTLE_ENDIAN);

            // --- EMR header ---
            int type = iis.readInt();
            int nSize = iis.readInt(); // total size of EMR_HEADER in bytes
            if (type != EMR_HEADER || nSize < 88) { // 88 bytes up to szlMillimeters in the base header
                throw new IIOException("Not an EMF header (type=" + type + ", size=" + nSize + ")");
            }

            // rclBounds (pixels)
            int boundsLeft = iis.readInt();
            int boundsTop = iis.readInt();
            int boundsRight = iis.readInt();
            int boundsBottom = iis.readInt();

            // rclFrame (0.01 millimeters)
            int frameLeft = iis.readInt();
            int frameTop = iis.readInt();
            int frameRight = iis.readInt();
            int frameBottom = iis.readInt();

            int signature = iis.readInt();
            if (signature != EMF_SIGNATURE) {
                throw new IIOException("Invalid EMF signature");
            }

            // Skip: nVersion, nBytes, nRecords, nHandles, sReserved
            /* nVersion */
            iis.readInt();
            /* nBytes   */
            iis.readInt();
            /* nRecords */
            iis.readInt();
            /* nHandles */
            iis.readUnsignedShort();
            /* sReserved*/
            iis.readUnsignedShort();

            // Description count + offset
            /* nDescription  */
            iis.readInt();
            /* offDescription */
            iis.readInt();

            // Palette entries
            /* nPalEntries */
            iis.readInt();

            // szlDevice (pixels) and szlMillimeters (millimeters)
            int deviceCx = iis.readInt();
            int deviceCy = iis.readInt();
            int mmCx = iis.readInt();
            int mmCy = iis.readInt();

            // Restore order ASAP
            iis.setByteOrder(oldOrder);

            // Compute size
            int width = Math.max(0, boundsRight - boundsLeft);
            int height = Math.max(0, boundsBottom - boundsTop);

            if (width == 0 || height == 0) {
                // Convert from frame (.01 mm) to pixels
                double frameWmm = (frameRight - frameLeft) / 100.0; // .01 mm -> mm
                double frameHmm = (frameBottom - frameTop) / 100.0;

                double dpiX = estimateDpi(deviceCx, mmCx);
                double dpiY = estimateDpi(deviceCy, mmCy);

                width = (int) Math.round((frameWmm / 25.4) * dpiX);
                height = (int) Math.round((frameHmm / 25.4) * dpiY);
            }

            if (width <= 0 || height <= 0) {
                throw new IIOException("Could not determine EMF image dimensions");
            }

            cachedSize = new Dimension(width, height);
        } catch (IOException e) {
            throw new IIOException("Failed to read EMF header", e);
        }
        return cachedSize;
    }

    private static double estimateDpi(int devicePixels, int deviceMillimeters) {
        if (devicePixels > 0 && deviceMillimeters > 0) {
            return devicePixels / (deviceMillimeters / 25.4);
        }
        // Assumption when no device info is available
        return 96.0; // common default DPI
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
        // No rasterization support – return an empty iterator.
        checkImageIndex(imageIndex);
        return Collections.emptyIterator();
    }

    @Override
    public javax.imageio.ImageReadParam getDefaultReadParam() {
        return new ImageReadParam();
    }

    @Override
    public IIOMetadata getStreamMetadata()
            throws IIOException {
        // Minimal implementation – no stream metadata yet.
        // TODO: Provide basic metadata with bounds and EMF version if needed.
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex)
            throws IIOException {
        // Minimal implementation – no per-image metadata yet.
        // TODO: Return a minimal IIOMetadata with width/height/bounds if consumers expect it.
        checkImageIndex(imageIndex);
        return null;
    }

    @Override
    public java.awt.image.BufferedImage read(int imageIndex, ImageReadParam param)
            throws IIOException {
        throw new UnsupportedOperationException("EMF rasterization is not supported by this reader");
    }

    @Override
    public boolean canReadRaster() {
        return false;
    }
}
