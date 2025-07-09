package pro.verron.officestamper.utils;

import pro.verron.officestamper.api.OfficeStamperException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.StringCharacterIterator;
import java.util.Base64;
import java.util.Locale;

/**
 * Utility class providing common operations for byte manipulation and conversions.
 * This class is not intended to be instantiated.
 */
public class ByteUtils {

    private ByteUtils() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    /// Computes the SHA-1 hash of the given input bytes and encodes the result in Base64.
    ///
    /// @param bytes the input byte array to be hashed.
    ///
    /// @return the SHA-1 hash of the input bytes, encoded in Base64.
    public static String sha1b64(byte[] bytes) {
        var messageDigest = findDigest();
        var encoder = Base64.getEncoder();
        var digest = messageDigest.digest(bytes);
        return encoder.encodeToString(digest);
    }

    private static MessageDigest findDigest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new OfficeStamperException(e);
        }
    }

    /**
     * Converts the size of a byte array into a human-readable string representation
     * using standard size prefixes (e.g., KB, MB, GB).
     *
     * @param imageBytes the input byte array whose size needs to be converted
     * @return a human-readable string representing the size of the byte array
     *         in appropriate units (e.g., "1.2KB", "3.4MB")
     */
    public static String readableSize(byte[] imageBytes) {
        double size = imageBytes.length;
        var prefixes = new StringCharacterIterator(" kMGTPE");
        while (size >= 1_000) {
            size /= 1_000;
            prefixes.next();
        }
        return String.format(Locale.ROOT, "%.1f%cB", size, prefixes.current());
    }
}
