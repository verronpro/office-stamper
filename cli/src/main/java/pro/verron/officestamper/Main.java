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
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.Files.newOutputStream;

@Command(name = "officestamper", mixinStandardHelpOptions = true, description = "Office Stamper CLI tool")
public class Main
        implements Runnable {

    private static final Logger logger = Utils.getLogger();

    @Option(names = {"-i", "--input"},
            required = true,
            description = "Input file path (csv, properties, html, xml, json, excel) or a keyword (diagnostic) for "
                          + "documented data sources") private String inputFile;

    @Option(names = {"-t", "--template"},
            required = true,
            description = "Template file path or a keyword (diagnostic) for documented template packages") private String templateFile;

    @Option(names = {"-o", "--output"},
            defaultValue = "output.docx",
            description = "Output file path") private String outputPath;

    @Option(names = {"-s", "--stamper"},
            defaultValue = "word",
            description = "Stamper type (word, powerpoint)") private String stamperType;

    public static void main(String[] args) {
        var main = new Main();
        var cli = new CommandLine(main);
        int exitCode = cli.execute(args);
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

        final var context = extractContext(inputFile);
        final var templateStream = extractTemplate(templateFile);
        final var outputStream = createOutputStream(Path.of(outputPath));

        final var stamper = switch (stamperType) {
            case "word" -> new WordStamper();
            case "powerpoint" -> new PowerPointStamper();
            default -> throw new OfficeStamperException("Invalid stamper type: " + stamperType);
        };

        stamper.stamp(context, templateStream, outputStream);
    }

    private Object extractContext(String input) {
        if ("diagnostic".equals(input)) return Diagnostic.context();
        return contextualise(Path.of(input));
    }

    private InputStream extractTemplate(String template) {
        if ("diagnostic".equals(template)) return Diagnostic.template();
        return streamFile(Path.of(template));
    }

    private OutputStream createOutputStream(Path path) {
        try {
            return newOutputStream(path);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object contextualise(Path path) {
        if (path.endsWith(".csv")) return processCsv(path);
        if (path.endsWith(".properties")) return processProperties(path);
        if (path.endsWith(".html") || path.endsWith(".xml")) return processXmlOrHtml(path);
        if (path.endsWith(".json")) return processJson(path);
        if (path.endsWith(".xlsx")) return processExcel(path);
        throw new OfficeStamperException("Unsupported file type: " + path);
    }

    private static InputStream streamFile(Path path) {
        try {
            return Files.newInputStream(path);
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
