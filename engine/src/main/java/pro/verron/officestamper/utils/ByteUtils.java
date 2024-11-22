package pro.verron.officestamper.utils;

import pro.verron.officestamper.api.OfficeStamperException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.StringCharacterIterator;
import java.util.Base64;
import java.util.Locale;

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

    /// Converts a byte count into a human-readable string using SI units.
    ///
    /// @param nb the byte quantity
    ///
    /// @return a human-readable string representation of the byte count.
    public static String humanReadableByteCountSI(long nb) {
        double size = nb;
        var ci = new StringCharacterIterator(" kMGTPE");
        while (size <= -1_000 || size >= 1_000) {
            size /= 1000;
            ci.next();
        }
        return String.format(Locale.ROOT, "%.1f%cB", size, ci.current());
    }
}
