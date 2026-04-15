package pro.verron.officestamper.imageio.emf;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.Iterator;

/**
 * Simple usage example:
 *
 * <pre>
 *   java EmfReaderExample path/to/image.emf
 * </pre>
 */
public final class EmfReaderExample {
    static void main(String[] args)
            throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: EmfReaderExample <file.emf>");
            return;
        }
        File f = new File(args[0]);
        try (ImageInputStream iis = ImageIO.createImageInputStream(f)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                System.out.println("No ImageIO reader found for: " + f);
                return;
            }
            ImageReader r = readers.next();
            r.setInput(iis, true, true);
            System.out.println("Reader: " + r.getClass()
                                             .getName() + " (format=" + r.getFormatName() + ")");
            System.out.println("Width : " + r.getWidth(0));
            System.out.println("Height: " + r.getHeight(0));
            r.dispose();
        }
    }
}
