package pro.verron.officestamper.imageio.emf;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/// Service Provider Interface (SPI) for the EMF ImageIO reader.
public final class EmfImageReaderSpi
        extends ImageReaderSpi {

    private static final String[] NAMES = {"emf"};
    private static final String[] SUFFIXES = {"emf"};
    private static final String[] MIMES = {"image/x-emf"};

    public EmfImageReaderSpi() {
        super("Office-stamper",
                "3.3",
                NAMES,
                SUFFIXES,
                MIMES,
                EmfImageReader.class.getName(),
                new Class[]{ImageInputStream.class},
                null,
                false,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null);
    }

    @Override
    public boolean canDecodeInput(Object source) {
        if (!(source instanceof ImageInputStream iis)) return false;
        long pos = 0L;
        try {
            pos = iis.getStreamPosition();
            // Read first 44 bytes and check for ' EMF' signature at offset 40
            byte[] header = new byte[44];
            int read = iis.read(header);
            iis.seek(pos);
            if (read < 44) return false;
            return header[40] == 0x20 && header[41] == 0x45 && header[42] == 0x4D && header[43] == 0x46; // ' EMF'
        } catch (IOException e) {
            try {iis.seek(pos);} catch (IOException ignore) {}
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new EmfImageReader(this);
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Minimal EMF metadata reader (width/height/bounds), no rasterization";
    }
}
