package pro.verron.officestamper.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/// IOStreams class.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.5
public class IOStreams {

    /// Constant <code>KEEP_OUTPUT_FILE=Boolean.parseBoolean(System.getenv()
    /// .getOrDefault(&quot;keepOutputFile&quot;, &quot;false&quot;))</code>
    private static final boolean KEEP_OUTPUT_FILE;
    private static final Map<OutputStream, Supplier<InputStream>> streams = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(IOStreams.class);

    static {
        var env = System.getenv();
        var keepOutputFile = env.getOrDefault("keepOutputFile", "false");
        KEEP_OUTPUT_FILE = Boolean.parseBoolean(keepOutputFile);
    }

    /// @return a [java.io.OutputStream] object
    ///
    /// @throws java.io.IOException if any.
    /// @since 1.6.6
    public static OutputStream getOutputStream()
            throws IOException {
        if (KEEP_OUTPUT_FILE) {
            Path temporaryFile = Files.createTempFile(TestDocxStamper.class.getSimpleName(), ".docx");
            logger.info("Saving DocxStamper output to temporary file {}", temporaryFile);
            OutputStream out = Files.newOutputStream(temporaryFile);
            ThrowingSupplier<InputStream> in = () -> Files.newInputStream(temporaryFile);
            streams.put(out, in);
            return out;
        }
        else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Supplier<InputStream> inSupplier = () -> new ByteArrayInputStream(out.toByteArray());
            streams.put(out, inSupplier);
            return out;
        }
    }

    /// @param out a [java.io.OutputStream] object
    ///
    /// @return a [java.io.InputStream] object
    ///
    /// @since 1.6.6
    public static InputStream getInputStream(OutputStream out) {
        return streams.get(out)
                      .get();
    }
}
