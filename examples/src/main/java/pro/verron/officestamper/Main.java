package pro.verron.officestamper;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.Files.newOutputStream;

@Command(name = "officestamper", mixinStandardHelpOptions = true, description = "Office Stamper CLI tool")
public class Main
        implements Runnable {

    private static final Logger logger = Utils.getLogger();

    @Option(names = {"-i", "--input-file"},
            required = true,
            description = "Input file path (csv, properties, html, xml, json, excel) or a keyword for documented data"
                          + " sources") private String inputFile;

    @Option(names = {"-t", "--template-file"},
            required = true,
            description = "Template file path or a keyword for documented template packages") private String templateFile;

    @Option(names = {"-o", "--output-path"},
            defaultValue = "default-output.docx",
            description = "Output file path") private String outputPath;

    @Option(names = {"-s", "--stamper"},
            defaultValue = "diagnostic",
            description = "Stamper type (diagnostic, powerpoint)") private String stamperType;

    public static void main(String[] args) {
        var main = new Main();
        int exitCode = new CommandLine(main).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (inputFile == null || templateFile == null) {
            logger.log(Level.SEVERE, "Input file and template file must be provided");
            return;
        }

        stamperType = stamperType.toLowerCase();

        logger.log(Level.INFO, "Input File: {}", inputFile);
        logger.log(Level.INFO, "Template File: {}", templateFile);
        logger.log(Level.INFO, "Output Path: {}", outputPath);
        logger.log(Level.INFO, "Stamper Type: {}", stamperType);

        final var context = switch (inputFile) {
            case "diagnostic" -> createDiagnosticContext();
            default -> contextualise(Path.of(inputFile));
        };

        final var templateStream = switch (templateFile) {
            case "diagnostic" -> loadDiagnosticTemplate();
            default -> streamFile(Path.of(templateFile));
        };

        final var outputStream = createOutputStream(Path.of(outputPath));

        final var stamper = switch (stamperType) {
            case "word" -> new WordStamper();
            case "powerpoint" -> new PowerPointStamper();
            default -> throw new OfficeStamperException("Invalid stamper type: " + stamperType);
        };

        stamper.stamp(context, templateStream, outputStream);
    }

    private static Object createDiagnosticContext() {
        logger.info("""
                Create a context with: \
                system environment variables, \
                jvm properties, \
                and user preferences""");
        var diagnosticMaker = new Diagnostic();
        var map = new TreeMap<String, Object>();
        map.put("reportDate", diagnosticMaker.date());
        map.put("reportUser", diagnosticMaker.user());
        map.put("environment", diagnosticMaker.environmentVariables());
        map.put("properties", diagnosticMaker.jvmProperties());
        map.put("preferences", diagnosticMaker.userPreferences());
        return map;
    }

    private Object contextualise(Path path) {
        String fileName = path.getFileName()
                              .toString()
                              .toLowerCase();
        if (fileName.endsWith(".csv")) return processCsv(path);
        if (fileName.endsWith(".properties")) return processProperties(path);
        if (fileName.endsWith(".html") || fileName.endsWith(".xml")) return processXmlOrHtml(path);
        if (fileName.endsWith(".json")) return processJson(path);
        if (fileName.endsWith(".xlsx")) return processExcel(path);
        throw new OfficeStamperException("Unsupported file type: " + fileName);
    }

    private static InputStream loadDiagnosticTemplate() {
        logger.info("Load the internally packaged 'Diagnostic.docx' template resource");
        return Utils.streamResource("Diagnostic.docx");
    }

    private static InputStream streamFile(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private OutputStream createOutputStream(Path path) {
        try {
            return newOutputStream(path);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processCsv(Path path) {
        throw new OfficeStamperException("Not yet implemented.");
    }

    private Object processProperties(Path path) {
        throw new OfficeStamperException("Not yet implemented.");
    }

    private Object processXmlOrHtml(Path path) {
        throw new OfficeStamperException("Not yet implemented.");
    }

    private Object processJson(Path path) {
        throw new OfficeStamperException("Not yet implemented.");
    }

    private Object processExcel(Path path) {
        try {
            return ExcelContext.from(Files.newInputStream(path));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }
}
