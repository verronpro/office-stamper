package pro.verron.officestamper.utils.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/// Utility for snapshot testing.
public final class SnapshotUtils {

    private SnapshotUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /// Asserts that the actual image matches the expected golden image.
    /// If the golden image doesn't exist, it creates it (useful for first run).
    ///
    /// @param actualPath path to the generated image
    /// @param goldenPath path to the golden image
    /// @param tolerance maximum allowed difference (0.0 to 1.0)
    /// @throws IOException if an I/O error occurs
    /// @throws AssertionError if images don't match
    public static void assertSnapshotMatch(Path actualPath, Path goldenPath, double tolerance)
            throws IOException {
        if (!Files.exists(goldenPath)) {
            Files.createDirectories(goldenPath.getParent());
            Files.copy(actualPath, goldenPath);
            System.out.println("[DEBUG_LOG] Created golden snapshot: " + goldenPath);
            return;
        }

        BufferedImage actual = ImageIO.read(actualPath.toFile());
        BufferedImage expected = ImageIO.read(goldenPath.toFile());

        double difference = calculateDifference(actual, expected);
        if (difference > tolerance) {
            throw new AssertionError(String.format(
                    "Snapshot mismatch for %s. Difference: %.4f (max tolerance: %.4f)",
                    goldenPath.getFileName(), difference, tolerance));
        }
    }

    /// Calculates the difference between two images.
    /// 0.0 means identical, 1.0 means completely different.
    public static double calculateDifference(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return 1.0;
        }

        long diff = 0;
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;
                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
            }
        }
        long maxDiff = 3L * 255 * img1.getWidth() * img1.getHeight();
        return (double) diff / maxDiff;
    }
}
