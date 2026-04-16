package pro.verron.officestamper.imageio.svg;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/// Minimal ImageIO reader for SVG that exposes only image dimensions.
///
/// Size resolution strategy:
///   - If `width` and `height` attributes are present on the root `<svg>` element,
///     parse them (supporting units: px, in, cm, mm, pt, pc). Convert to pixels assuming 96 DPI.
///   - Else, if `viewBox="minX minY width height"` is present, use the width/height in user units
///     as pixels (CSS: 1 user unit equals 1 px at 96 DPI).
///   - Else, throw an [IIOException] as dimensions cannot be determined safely.
public final class SvgImageReader
        extends ImageReader {

    private static final Pattern ROOT_TAG_PATTERN = compile("<svg(?:[\n\r\t :][^>]*)?>", CASE_INSENSITIVE);
    private static final Pattern WIDTH_ATTR = compile("\\bwidth\\s*=\\s*\"([^\"]+)\"", CASE_INSENSITIVE);
    private static final Pattern HEIGHT_ATTR = compile("\\bheight\\s*=\\s*\"([^\"]+)\"", CASE_INSENSITIVE);
    private static final Pattern VIEWBOX_ATTR = compile("\\bviewBox\\s*=\\s*\"([^\"]+)\"", CASE_INSENSITIVE);

    private Dimension cachedSize;

    SvgImageReader(SvgImageReaderSpi spi) {
        super(spi);
    }

    @Override
    public String getFormatName() {
        return "svg";
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
        if (imageIndex != 0) throw new IIOException("SVG reader supports a single image (index 0)");
        ensureInputSet();
    }

    private Dimension getOrParseSize()
            throws IIOException {
        if (cachedSize != null) return cachedSize;
        var iis = (ImageInputStream) getInput();
        long pos = 0L;
        try {
            pos = iis.getStreamPosition();
            byte[] buf = new byte[8192];
            int n = iis.read(buf);
            iis.seek(pos);
            if (n <= 0) throw new IIOException("Empty SVG stream");

            String prefix = new String(buf, 0, n, StandardCharsets.UTF_8);
            Matcher root = ROOT_TAG_PATTERN.matcher(prefix);
            if (!root.find()) {
                throw new IIOException("Could not locate <svg> root element");
            }
            String rootTag = root.group();

            Integer widthPx = tryParseCssPixels(extractAttr(rootTag, WIDTH_ATTR));
            Integer heightPx = tryParseCssPixels(extractAttr(rootTag, HEIGHT_ATTR));

            if (widthPx != null && heightPx != null) {
                cachedSize = new Dimension(widthPx, heightPx);
                return cachedSize;
            }

            String viewBox = extractAttr(rootTag, VIEWBOX_ATTR);
            if (viewBox != null) {
                String[] parts = viewBox.split("[\n\r\t ]+");
                if (parts.length == 4) {
                    double vbW = parseNumber(parts[2]);
                    double vbH = parseNumber(parts[3]);
                    int w = (int) Math.round(vbW);
                    int h = (int) Math.round(vbH);
                    if (w > 0 && h > 0) {
                        cachedSize = new Dimension(w, h);
                        return cachedSize;
                    }
                }
            }

            throw new IIOException("Could not determine SVG image dimensions");
        } catch (IOException e) {
            try {iis.seek(pos);} catch (IOException ignore) {}
            throw new IIOException("Failed to parse SVG header", e);
        }
    }

    private static Integer tryParseCssPixels(String value) {
        if (value == null) return null;
        String v = value.toLowerCase(Locale.ROOT);
        try {
            Map<String, Integer> unitToPixels = new HashMap<>();
            unitToPixels.put("px", 1);
            unitToPixels.put("in", 96);
            unitToPixels.put("cm", (int) (96.0 / 2.54));
            unitToPixels.put("mm", (int) (96.0 / 25.4));
            unitToPixels.put("pt", (int) (96.0 / 72.0)); // 1pt = 1/72in
            unitToPixels.put("pc", 12); // 1pc = 12pt

            for (var e : unitToPixels.entrySet()) {
                var unit = e.getKey();
                var pixelRatio = e.getValue();
                var index = v.indexOf(unit);
                if (index > -1) return (int) Math.round(parseNumber(v.substring(0, index)) * pixelRatio);
            }
            // Unitless: CSS says unitless length in <svg> is in user units; treat as px in common practice.
            return (int) Math.round(parseNumber(v));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String extractAttr(String tag, Pattern pattern) {
        Matcher m = pattern.matcher(tag);
        if (m.find()) {
            var group = m.group(1);
            return group.trim();
        }
        return null;
    }

    private static double parseNumber(String s) {
        return Double.parseDouble(s.trim());
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
        throw new UnsupportedOperationException("SVG rasterization is not supported by this reader");
    }
}
