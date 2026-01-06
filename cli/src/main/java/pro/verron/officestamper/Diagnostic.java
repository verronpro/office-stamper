package pro.verron.officestamper;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.util.stream.Collectors.toMap;

public final class Diagnostic {

    private static final Logger logger = Utils.getLogger();
    private final LocalDate date;
    private final String user;
    private final Map<String, String> userPreferences;
    private final Map<String, String> jvmProperties;
    private final Map<String, String> environmentVariables;

    private Diagnostic() {
        this(
                LocalDate.now(),
                System.getenv("USERNAME"),
                extractUserPreferences(),
                extractJvmProperties(),
                extractEnvironmentVariables()
        );
    }

    private Diagnostic(
            LocalDate date,
            String user,
            Map<String, String> userPreferences,
            Map<String, String> jvmProperties,
            Map<String, String> environmentVariables
    ) {
        this.date = date;
        this.user = user;
        this.userPreferences = userPreferences;
        this.jvmProperties = jvmProperties;
        this.environmentVariables = environmentVariables;
    }

    private static Map<String, String> extractUserPreferences() {
        var preferenceRoot = Preferences.userRoot();
        var preferenceKeys = extractPreferenceKeys(preferenceRoot);
        var entries = preferenceKeys.stream()
                                    .collect(toMap(key -> key, k -> preferenceRoot.get(k, "<null>")));
        return Map.copyOf(entries);
    }

    private static Map<String, String> extractJvmProperties() {
        var properties = System.getProperties();
        var propertyNames = properties.stringPropertyNames();
        var entries = propertyNames.stream()
                                   .collect(toMap(k -> k, properties::getProperty));
        return Map.copyOf(entries);
    }

    private static Map<String, String> extractEnvironmentVariables() {
        var env = System.getenv();
        return Map.copyOf(env);
    }

    private static List<String> extractPreferenceKeys(Preferences preferenceRoot) {
        try {
            return Arrays.asList(preferenceRoot.keys());
        } catch (BackingStoreException e) {
            logger.log(Level.WARNING, "Failed to list the preference keys", e);
            return List.of("failed-to-list-preference-keys");
        }
    }

    static Object context() {
        return context(new Diagnostic());
    }

    private static Object context(Diagnostic diagnosticMaker) {
        logger.info("Create a context with system environment variables, jvm properties, and user preferences");
        var map = new TreeMap<String, Object>();
        map.put("reportDate", diagnosticMaker.date());
        map.put("reportUser", diagnosticMaker.user());
        map.put("environment",
                diagnosticMaker.environmentVariables()
                               .entrySet());
        map.put("properties",
                diagnosticMaker.jvmProperties()
                               .entrySet());
        map.put("preferences",
                diagnosticMaker.userPreferences()
                               .entrySet());
        return map;
    }

    public LocalDate date() {return date;}

    public String user() {return user;}

    private Map<String, String> environmentVariables() {
        return environmentVariables;
    }

    private Map<String, String> jvmProperties() {
        return jvmProperties;
    }

    private Map<String, String> userPreferences() {
        return userPreferences;
    }

    static Object context(
            LocalDate date,
            String user,
            Map<String, String> userPreferences,
            Map<String, String> jvmProperties,
            Map<String, String> environmentVariables
    ) {
        return context(new Diagnostic(date, user, userPreferences, jvmProperties, environmentVariables));
    }

    static InputStream template() {
        logger.info("Load the internally packaged 'Diagnostic.docx' template resource");
        return Utils.streamResource("Diagnostic.docx");
    }
}
