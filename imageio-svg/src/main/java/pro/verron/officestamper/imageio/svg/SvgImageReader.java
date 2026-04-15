package pro.verron.officestamper.imageio.svg;

import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal ImageIO reader for SVG that exposes only image dimensions.
 *
 * <p>
 * Size resolution strategy:
 * </p>
 * <ul>
 *   <li>If {@code width} and {@code height} attributes are present on the root {@code <svg>} element,
 *   parse them (supporting units: px, in, cm, mm, pt, pc). Convert to pixels assuming 96 DPI.</li>
 *   <li>Else, if {@code viewBox="minX minY width height"} is present, use the width/height in user units
 *   as pixels (CSS: 1 user unit equals 1 px at 96 DPI).</li>
 *   <li>Else, throw an {@link IIOException} as dimensions cannot be determined safely.</li>
 * </ul>
 */
public final class SvgImageReader
        extends ImageReader {

    private static final Pattern ROOT_TAG_PATTERN = Pattern.compile("<svg(?:[\n\r\t :][^>]*)?>",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern WIDTH_ATTR = Pattern.compile("\\bwidth\\s*=\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern HEIGHT_ATTR = Pattern.compile("\\bheight\\s*=\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern VIEWBOX_ATTR = Pattern.compile("\\bviewBox\\s*=\\s*\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE);

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
                String[] parts = viewBox.trim()
                                        .split("[\n\r\t ]+");
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
        String v = value.trim()
                        .toLowerCase(Locale.ROOT);
        try {
            if (v.endsWith("px")) {
                return (int) Math.round(parseNumber(v.substring(0, v.length() - 2)));
            }
            else if (v.endsWith("in")) {
                return (int) Math.round(parseNumber(v.substring(0, v.length() - 2)) * 96.0);
            }
            else if (v.endsWith("cm")) {
                return (int) Math.round(parseNumber(v.substring(0, v.length() - 2)) * (96.0 / 2.54));
            }
            else if (v.endsWith("mm")) {
                return (int) Math.round(parseNumber(v.substring(0, v.length() - 2)) * (96.0 / 25.4));
            }
            else if (v.endsWith("pt")) { // 1pt = 1/72in
                return (int) Math.round(parseNumber(v.substring(0, v.length() - 2)) * (96.0 / 72.0));
            }
            else if (v.endsWith("pc")) { // 1pc = 12pt
                return (int) Math.round(parseNumber(v.substring(0, v.length() - 2)) * (96.0 / 72.0) * 12.0);
            }
            else {
                // Unitless: CSS says unitless length in <svg> is in user units; treat as px in common practice.
                return (int) Math.round(parseNumber(v));
            }
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String extractAttr(String tag, Pattern pattern) {
        Matcher m = pattern.matcher(tag);
        return m.find() ? m.group(1) : null;
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
    public IIOMetadata getStreamMetadata()
            throws IIOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex)
            throws IIOException {
        checkImageIndex(imageIndex);
        return null;
    }

    @Override
    public java.awt.image.BufferedImage read(int imageIndex, ImageReadParam param)
            throws IIOException {
        throw new UnsupportedOperationException("SVG rasterization is not supported by this reader");
    }
}
