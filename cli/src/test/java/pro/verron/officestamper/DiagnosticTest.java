package pro.verron.officestamper;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiagnosticTest {

    /// Test class for the [Diagnostic#context()] method. This method creates a report of system information including
    /// environment variables, JVM properties, and user preferences.
    @Test
    void testContextContainsReportDate() {
        // Act
        Object context = Diagnostic.context();

        // Assert
        assertInstanceOf(TreeMap.class, context, "Context should be an instance of TreeMap");
        var map = (TreeMap<String, Object>) context;

        assertTrue(map.containsKey("reportDate"), "Context should contain 'reportDate'");
        assertEquals(LocalDate.now(), map.get("reportDate"), "reportDate should be today's date");
    }

    @Test
    void testContextContainsReportUser() {
        // Mock
        String expectedUser = "testuser";
        var environmentMock = mockStatic(System.class);
        environmentMock.when(() -> System.getenv("USERNAME"))
                       .thenReturn(expectedUser);

        // Act
        Object context = Diagnostic.context();

        // Assert
        assertInstanceOf(TreeMap.class, context, "Context should be an instance of TreeMap");
        var map = (TreeMap<String, Object>) context;

        assertTrue(map.containsKey("reportUser"), "Context should contain 'reportUser'");
        assertEquals(expectedUser, map.get("reportUser"), "reportUser should match the mocked username");

        environmentMock.close();
    }

    @Test
    void testContextContainsEnvironmentVariables() {
        // Mock
        var environmentMock = mockStatic(System.class);
        var testEnvironment = Map.of("VAR1", "VALUE1", "VAR2", "VALUE2");
        environmentMock.when(System::getenv)
                       .thenReturn(testEnvironment);

        // Act
        Object context = Diagnostic.context();

        // Assert
        assertInstanceOf(TreeMap.class, context, "Context should be an instance of TreeMap");
        var map = (TreeMap<String, Object>) context;

        assertTrue(map.containsKey("environment"), "Context should contain 'environment'");
        var environment = map.get("environment");
        assertNotNull(environment, "Environment variables should not be null");
        assertInstanceOf(Iterable.class, environment, "Environment should be iterable");
        assertIterableEquals(testEnvironment.entrySet(),
                (Iterable<?>) environment,
                "Environment variables should match");

        environmentMock.close();
    }

    @Test
    void testContextContainsJvmProperties() {
        // Mock
        var propertiesMock = mockStatic(System.class);
        var testProperties = new TreeMap<>(Map.of("prop1", "value1", "prop2", "value2"));
        propertiesMock.when(System::getProperties)
                      .thenReturn(testProperties);

        // Act
        Object context = Diagnostic.context();

        // Assert
        assertInstanceOf(TreeMap.class, context, "Context should be an instance of TreeMap");
        var map = (TreeMap<String, Object>) context;

        assertTrue(map.containsKey("properties"), "Context should contain 'properties'");
        var properties = map.get("properties");
        assertNotNull(properties, "JVM properties should not be null");
        assertInstanceOf(Iterable.class, properties, "Properties should be iterable");
        assertIterableEquals(testProperties.entrySet(), (Iterable<?>) properties, "JVM properties should match");

        propertiesMock.close();
    }

    @Test
    void testContextContainsUserPreferences() {
        // Mock
        var preferencesMock = mockStatic(Preferences.class);
        var userRootMock = mock(Preferences.class);
        preferencesMock.when(Preferences::userRoot)
                       .thenReturn(userRootMock);

        try {
            when(userRootMock.keys()).thenReturn(new String[]{"key1", "key2"});
            when(userRootMock.get("key1", "<null>")).thenReturn("value1");
            when(userRootMock.get("key2", "<null>")).thenReturn("value2");

            // Act
            Object context = Diagnostic.context();

            // Assert
            assertInstanceOf(TreeMap.class, context, "Context should be an instance of TreeMap");
            var map = (TreeMap<String, Object>) context;

            assertTrue(map.containsKey("preferences"), "Context should contain 'preferences'");
            var preferences = map.get("preferences");
            assertNotNull(preferences, "User preferences should not be null");
            assertInstanceOf(Iterable.class, preferences, "Preferences should be iterable");
            assertIterableEquals(Map.of("key1", "value1", "key2", "value2")
                                    .entrySet(),
                    (Iterable<?>) preferences, "Preferences should match");
        } catch (Exception e) {
            fail("Exception should not be thrown during test: " + e.getMessage());
        } finally {
            preferencesMock.close();
        }
    }
}
