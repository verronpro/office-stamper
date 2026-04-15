package pro.verron.officestamper.imageio.wmf;

import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/**
 * Minimal ImageIO SPI for WMF (Windows Metafile).
 *
 * <p>
 * It recognizes Placeable WMF files by their magic key {@code 0x9AC6CDD7} and
 * creates a {@link WmfImageReader} that exposes only dimensions (no rasterization).
 * </p>
 */
public final class WmfImageReaderSpi
        extends ImageReaderSpi {

    private static final String[] NAMES = {"wmf"};
    private static final String[] SUFFIXES = {"wmf"};
    private static final String[] MIMES = {"image/x-wmf", "image/wmf", "application/x-msmetafile"};

    public WmfImageReaderSpi() {
        super(
                "Office-stamper",
                "3.3",
                NAMES,
                SUFFIXES,
                MIMES,
                WmfImageReader.class.getName(),
                new Class[]{ImageInputStream.class},
                null,
                false, null, null, null, null,
                false, null, null, null, null
        );
    }

    @Override
    public boolean canDecodeInput(Object source)
            throws IIOException {
        if (!(source instanceof ImageInputStream iis)) return false;
        long pos;
        try {
            pos = iis.getStreamPosition();
            // Placeable WMF header is 22 bytes; first 4 are 0x9AC6CDD7 little-endian
            byte[] header = new byte[4];
            int read = iis.read(header);
            iis.seek(pos);
            if (read < 4) return false;
            return (header[0] == (byte) 0xD7
                    && header[1] == (byte) 0xCD
                    && header[2] == (byte) 0xC6
                    && header[3] == (byte) 0x9A);
        } catch (IOException e) {
            try {iis.seek(0);} catch (IOException ignore) {}
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension)
            throws IIOException {
        return new WmfImageReader(this);
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Minimal WMF metadata reader (width/height via Placeable header); no rasterization";
    }
}
