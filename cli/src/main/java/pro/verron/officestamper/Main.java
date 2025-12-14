package pro.verron.officestamper;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.ExperimentalStampers;
import pro.verron.officestamper.preset.OfficeStampers;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    static void main(String[] args) {
        var main = new Main();
        var cli = new CommandLine(main);
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    private static InputStream streamFile(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
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
            case "word" -> OfficeStampers.docxStamper();
            case "powerpoint" -> ExperimentalStampers.pptxStamper();
            default -> throw new OfficeStamperException("Invalid stamper type: " + stamperType);
        };

        stamper.stamp(templateStream, context, outputStream);
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

    /// Return a list of objects with the csv properties
    private Object processCsv(Path path) {
        try (var reader = new CSVReader(new InputStreamReader(Files.newInputStream(path)))) {
            String[] headers = reader.readNext();
            return reader.readAll()
                         .stream()
                         .map(row -> {
                             Map<String, String> map = new LinkedHashMap<>();
                             for (int i = 0; i < headers.length; i++) {
                                 map.put(headers[i], row[i]);
                             }
                             return map;
                         })
                         .toList();
        } catch (IOException | CsvException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processProperties(Path path) {
        var properties = new Properties();
        try (var inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            return new LinkedHashMap<>(properties.entrySet()
                                                 .stream()
                                                 .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                                                         e -> String.valueOf(e.getValue()),
                                                         (a, b) -> b,
                                                         LinkedHashMap::new)));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processXmlOrHtml(Path path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(Files.newInputStream(path));
            return processNode(document.getDocumentElement());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Map<String, Object> processNode(Element element) {
        Map<String, Object> result = new LinkedHashMap<>();
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element childElement) {
                String name = childElement.getTagName();
                if (childElement.hasChildNodes() && childElement.getFirstChild()
                                                                .getNodeType() != Node.TEXT_NODE) {
                    result.put(name, processNode(childElement));
                }
                else {
                    result.put(name, childElement.getTextContent());
                }
            }
        }
        return result;
    }

    private Object processJson(Path path) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<>() {};
            return mapper.readValue(Files.newInputStream(path), typeRef);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processExcel(Path path) {
        try {
            return ExcelContext.from(Files.newInputStream(path));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }
}
