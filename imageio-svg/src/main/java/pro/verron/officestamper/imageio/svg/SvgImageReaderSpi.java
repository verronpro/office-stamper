package pro.verron.officestamper.imageio.svg;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/// Minimal ImageIO SPI for SVG.
///
/// Detects an SVG if the stream prefix contains a `<svg` root element (case-insensitive),
/// allowing optional XML declarations and whitespace.
public final class SvgImageReaderSpi
        extends ImageReaderSpi {

    private static final String[] NAMES = {"svg"};
    private static final String[] SUFFIXES = {"svg"};
    private static final String[] MIMES = {"image/svg+xml"};

    public SvgImageReaderSpi() {
        super("Office-stamper",
                "3.3",
                NAMES,
                SUFFIXES,
                MIMES,
                SvgImageReader.class.getName(),
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
        long pos;
        try {
            pos = iis.getStreamPosition();
            byte[] buf = new byte[2048];
            int n = iis.read(buf);
            iis.seek(pos);
            if (n <= 0) return false;
            String s = new String(buf, 0, n, StandardCharsets.UTF_8).toLowerCase();
            // Skip XML declaration if any and test presence of <svg
            int idx = s.indexOf("<svg");
            if (idx >= 0) return true;
            // Some files may declare namespace prefix: <svg:svg ...>
            return s.contains("<svg:svg");
        } catch (IOException e) {
            try {iis.seek(0);} catch (IOException ignore) {}
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object extension) {
        return new SvgImageReader(this);
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Minimal SVG metadata reader (width/height via attributes or viewBox); no rasterization";
    }
}
