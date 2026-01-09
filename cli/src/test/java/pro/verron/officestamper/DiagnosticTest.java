package pro.verron.officestamper;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

/// The DiagnosticTest class contains unit test methods to validate the behavior of the Diagnostic utility and ensure
/// the correctness of its context generation capabilities. These tests cover properties such as report date, report
/// user, environment variables, JVM properties, and user preferences. This class leverages JUnit test cases to test the
/// Diagnostic.context() method under various conditions and scenarios, including default and custom configurations. Key
/// test methods verify:
/// 1. The inclusion of a 'reportDate' field with the correct value.
/// 2. The presence and correctness of a 'reportUser' field.
/// 3. Validation of environment variables within the context.
/// 4. Validation of JVM properties within the context.
/// 5. Presence and validation of user preferences in the context.
/// 6. Custom context values provided through method parameters. Assertions are used throughout to ensure the context
/// data matches expectations, utilizing both default values and custom-configured values for all supported fields.
public class DiagnosticTest {

    /// Default constructor.
    public DiagnosticTest() {
    }

    @Test
    void testContextContainsReportDate() {
        // Act
        var context = (Map<String, Object>) Diagnostic.context();

        // Assert
        assertTrue(context.containsKey("reportDate"), "Context should contain 'reportDate'");
        assertEquals(LocalDate.now(), context.get("reportDate"), "reportDate should be today's date");
    }

    @Test
    void testContextContainsReportUser() {
        // Act
        var context = (Map<String, Object>) Diagnostic.context();

        // Assert
        assertTrue(context.containsKey("reportUser"), "Context should contain 'reportUser'");
        assertEquals(System.getenv("USERNAME"),
                context.get("reportUser"),
                "reportUser should match the actual username");
    }

    @Test
    void testContextContainsEnvironmentVariables() {
        // Act
        var context = (Map<String, Object>) Diagnostic.context();

        // Assert
        assertTrue(context.containsKey("environment"), "Context should contain 'environment'");
        var environment = context.get("environment");
        assertNotNull(environment, "Environment variables should not be null");
        assertInstanceOf(Iterable.class, environment, "Environment should be iterable");

        var environmentMap = new TreeMap<String, String>();
        for (Object o : (Iterable<?>) environment) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
            environmentMap.put(entry.getKey(), entry.getValue());
        }

        var actualEnv = new TreeMap<>(System.getenv());
        assertEquals(actualEnv, environmentMap, "Environment variables should match actual environment");
    }

    @Test
    void testContextContainsJvmProperties() {
        // Act
        var context = (Map<String, Object>) Diagnostic.context();

        // Assert
        assertTrue(context.containsKey("properties"), "Context should contain 'properties'");
        var properties = context.get("properties");
        assertNotNull(properties, "JVM properties should not be null");
        assertInstanceOf(Iterable.class, properties, "Properties should be iterable");

        var propertiesMap = new TreeMap<String, String>();
        for (Object o : (Iterable<?>) properties) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) o;
            propertiesMap.put(entry.getKey(), entry.getValue());
        }

        var actualProps = new TreeMap<String, String>();
        System.getProperties()
              .stringPropertyNames()
              .forEach(k -> actualProps.put(k, System.getProperty(k)));
        assertEquals(actualProps, propertiesMap, "JVM properties should match actual properties");
    }

    @Test
    void testContextContainsUserPreferences() {
        // Act
        var context = (Map<String, Object>) Diagnostic.context();

        // Assert
        assertTrue(context.containsKey("preferences"), "Context should contain 'preferences'");
        var preferences = context.get("preferences");
        assertNotNull(preferences, "User preferences should not be null");
        assertInstanceOf(Iterable.class, preferences, "Preferences should be iterable");
    }

    @Test
    void testContextWithCustomValues() {
        // Arrange
        LocalDate date = LocalDate.of(2023, 1, 1);
        String user = "customUser";
        Map<String, String> preferences = Map.of("pref1", "val1");
        Map<String, String> properties = Map.of("prop1", "val1");
        Map<String, String> environment = Map.of("env1", "val1");

        // Act
        var context = (Map<String, Object>) Diagnostic.context(date, user, preferences, properties, environment);

        // Assert
        assertEquals(date, context.get("reportDate"));
        assertEquals(user, context.get("reportUser"));
        assertIterableEquals(preferences.entrySet(), (Iterable<?>) context.get("preferences"));
        assertIterableEquals(properties.entrySet(), (Iterable<?>) context.get("properties"));
        assertIterableEquals(environment.entrySet(), (Iterable<?>) context.get("environment"));
    }
}
