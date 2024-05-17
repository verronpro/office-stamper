package pro.verron.officestamper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.util.stream.Collectors.toMap;

public class Diagnostic {

    public static final Logger logger = Utils.getLogger();

    private final LocalDate date;
    private final String user;

    public Diagnostic() {
        date = LocalDate.now();
        user = System.getenv("USERNAME");
    }

    LocalDate date() {
        return date;
    }

    String user() {
        return user;
    }

    List<Entry<String, String>> userPreferences() {
        var preferenceRoot = Preferences.userRoot();
        var preferenceKeys = extractPreferenceKeys(preferenceRoot);
        var entries = preferenceKeys.stream()
                                    .collect(toMap(k -> k, k -> preferenceRoot.get(k, null)))
                                    .entrySet();
        return List.copyOf(entries);
    }

    private List<String> extractPreferenceKeys(Preferences preferenceRoot) {
        try {
            return Arrays.asList(preferenceRoot.keys());
        } catch (BackingStoreException e) {
            logger.log(Level.WARNING, "Failed to list the preference keys", e);
            return List.of("failed-to-list-preference-keys");
        }
    }

    List<Entry<String, String>> jvmProperties() {
        var properties = System.getProperties();
        var propertyNames = properties.stringPropertyNames();
        var entries = propertyNames.stream()
                                   .collect(toMap(k -> k, properties::getProperty))
                                   .entrySet();
        return List.copyOf(entries);
    }

    List<Entry<String, String>> environmentVariables() {
        var env = System.getenv();
        var entries = env.entrySet();
        return List.copyOf(entries);
    }

}
