package pro.verron.officestamper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Traceability Report Tests") class TraceabilityTest {

    @TempDir Path tempDir;

    @Test
    @DisplayName("Should generate JSON traceability report")
    void testTraceabilityReport()
            throws IOException {
        var dataFile = tempDir.resolve("data.json");
        Files.writeString(dataFile, """
                {
                    "reportUser": "Alice",
                    "reportDate": "28/05/2026",
                    "environment": [],
                    "properties": [],
                    "preferences": []
                }""");

        var reportFile = tempDir.resolve("traceability.json");
        var outputFile = tempDir.resolve("output.docx");

        var main = new Main();
        picocli.CommandLine cli = new CommandLine(main);

        int exitCode = cli.execute("--template",
                "diagnostic",
                "--data",
                dataFile.toString(),
                "--output",
                outputFile.toString(),
                "--report",
                reportFile.toString(),
                "--dry-run");

        assertEquals(0, exitCode, "CLI should exit successfully");
        assertTrue(Files.exists(reportFile),
                "Traceability report should be created");

        var mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());

        var typeRef = new TypeReference<TraceabilityReport>() {};
        var report = mapper.readValue(reportFile.toFile(), typeRef);

        var resolutions = report.getResolutions();
        assertFalse(resolutions.isEmpty(), "Report should contain resolutions");
        assertTrue(resolutions.stream()
                              .anyMatch(r -> r.expression()
                                              .contains("reportUser")),
                "Report should contain 'reportUser' resolution");
    }

    @Test
    @DisplayName("Should generate HTML viewer from JSON report")
    void testReportView()
            throws IOException {
        Path reportFile = tempDir.resolve("traceability.json");
        Files.writeString(reportFile, """
                [
                  {
                    "expression": "name",
                    "value": "Alice",
                    "contextStack": ["{name=Alice, age=30}"]
                  }
                ]
                """);

        Path htmlFile = tempDir.resolve("traceability.html");

        Main main = new Main();
        picocli.CommandLine cli = new CommandLine(main);

        int exitCode = cli.execute("report-view",
                "--input",
                reportFile.toString(),
                "--output",
                htmlFile.toString());

        assertEquals(0, exitCode, "CLI should exit successfully");
        assertTrue(Files.exists(htmlFile), "HTML viewer should be created");

        String htmlContent = Files.readString(htmlFile);
        assertTrue(htmlContent.contains("Alice"),
                "HTML should contain resolved value");
        assertTrue(htmlContent.contains("name"),
                "HTML should contain expression");
    }
}
