package pro.verron.officestamper;

import pro.verron.officestamper.api.OfficeStamperException;

import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

/// Utility class for the CLI.
public class Utils {

    /// Logging format property key.
    public static final String LOGGING_FORMAT_KEY = "java.util.logging.SimpleFormatter.format";
    /// Logging format property value.
    public static final String LOGGING_FORMAT_VAL = "[%1$tl:%1$tM:%1$tS] %2$s %4$-7s: %5$s %6$s %n";

    static {
        Locale.setDefault(Locale.ROOT);
        System.setProperty(LOGGING_FORMAT_KEY, LOGGING_FORMAT_VAL);
        Logger.getLogger("org.docx4j")
              .setLevel(Level.SEVERE);
    }

    private Utils() {
        throw new OfficeStamperException("Utility class");
    }

    /// Returns a logger for the caller class.
    ///
    /// @return a logger
    public static Logger getLogger() {
        var callerName = StackWalker
                .getInstance(RETAIN_CLASS_REFERENCE)
                .getCallerClass()
                .getTypeName();
        return Logger.getLogger(callerName);
    }

    static InputStream streamResource(String name) {
        return StackWalker
                .getInstance(RETAIN_CLASS_REFERENCE)
                .getCallerClass()
                .getResourceAsStream(name);
    }

}
