package pro.verron.officestamper;

import pro.verron.officestamper.api.OfficeStamperException;

import java.io.InputStream;
import java.util.Locale;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

/// Utility class for the CLI.
public class Utils {

    static {
        Locale.setDefault(Locale.ROOT);
    }

    private Utils() {
        throw new OfficeStamperException("Utility class");
    }

    static InputStream streamResource(String name) {
        return StackWalker.getInstance(RETAIN_CLASS_REFERENCE)
                          .getCallerClass()
                          .getResourceAsStream(name);
    }

}
