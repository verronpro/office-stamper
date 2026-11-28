package pro.verron.officestamper.test;

import java.util.ServiceLoader;

/// Locator for the [TestFlavor] of the docx4j flavor under test.
public final class Flavor {
    private static volatile TestFlavor instance;

    private Flavor() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    /// Returns the flavor installed on the current test classpath.
    public static TestFlavor get() {
        var result = instance;
        if (result == null) {
            synchronized (Flavor.class) {
                result = instance;
                if (result == null) {
                    instance = result = ServiceLoader.load(TestFlavor.class)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "No TestFlavor registered on the test classpath"));
                }
            }
        }
        return result;
    }
}
