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
import pro.verron.officestamper.excel.ExcelContext;
import pro.verron.officestamper.experimental.ExperimentalStampers;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
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

/// Main class for the CLI.
@Command(name = "officestamper", mixinStandardHelpOptions = true, description = "Office Stamper CLI tool")
public class Main
        implements Runnable {

    private static final Logger logger = Utils.getLogger();

    // Flags: template (-t/--template) and data (-d/--data)
    @Option(names = {"-t", "--template"},
            required = true,
            description = "Template file path (.docx|.pptx), or a keyword (diagnostic) for a packaged sample template")
    private String templatePath;

    @Option(names = {"-d", "--data"},
            required = false,
            description = "Data input: file (json|yaml|yml|properties|csv|xlsx|xml|html), a directory, or 'diagnostic'")
    private String dataPath;

    @Option(names = {"-o", "--output"},
            defaultValue = "output.docx",
            description = "Output file path")
    private String outputPath;

    @Option(names = {"--dry-run"},
            description = "Validate template + data and variables, but do not produce the output file")
    private boolean dryRun;

    @Option(names = {"--report"},
            description = "Optional JSON report file path with run metadata and validation results")
    private String reportPath;

    @Option(names = {"--log-format"},
            defaultValue = "human",
            description = "Logging format: 'human' (default) or 'json' (structured logs to stdout)")
    private String logFormat;

    /// Default constructor.
    public Main() {
    }

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

    private static boolean isSupportedDataFile(Path p) {
        var n = p.getFileName()
                 .toString()
                 .toLowerCase();
        return n.endsWith(".csv") || n.endsWith(".properties") || n.endsWith(".html") ||
               n.endsWith(".xml") || n.endsWith(".json") || n.endsWith(".yaml") ||
               n.endsWith(".yml") || n.endsWith(".xlsx");
    }

    private static String baseName(Path f) {
        var base = f.getFileName()
                    .toString();
        var idx = base.lastIndexOf('.');
        if (idx > 0) base = base.substring(0, idx);
        return base;
    }

    @Override
    public void run() {
        // Normalize log format
        var lf = logFormat == null
                ? "human"
                : logFormat.trim()
                           .toLowerCase();

        // Basic CLI validation according to new flags
        if (templatePath == null || templatePath.isBlank()) {
            emit("ERROR", "Missing required --template path", null, lf);
            throw new CommandLine.ParameterException(new CommandLine(this), "--template is required");
        }
        if ((dataPath == null || dataPath.isBlank()) && !"diagnostic".equals(templatePath)) {
            emit("ERROR", "Missing required --data when not using diagnostic template", null, lf);
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "--data is required when template != diagnostic");
        }

        emit("INFO", "Start", Map.of(
                "template", templatePath,
                "data", dataPath == null ? "<none>" : dataPath,
                "output", outputPath,
                "dryRun", dryRun
        ), lf);

        try {
            var ext = templateKind(templatePath);
            // Folder semantics: each top-level file is its own context and yields one output;
            // each top-level subfolder merges its files (recursively) into a bigger context and yields one output.
            if (dataPath != null && !dataPath.isBlank() && Files.isDirectory(Path.of(dataPath))) {
                var items = buildItemsFromDataDirectory(Path.of(dataPath));
                var results = new java.util.ArrayList<RunResult>(items.size());
                int idx = 0;
                for (var item : items) {
                    idx++;
                    emit("INFO", "Processing item", Map.of("index", idx, "name", item.name, "total", items.size()), lf);
                    try (var templateStream = extractTemplateNew(templatePath)) {
                        if (dryRun) {
                            var configuration = OfficeStamperConfigurations.standard()
                                                                           .setExceptionResolver(pro.verron.officestamper.preset.ExceptionResolvers.throwing());
                            switch (ext) {
                                case WORD -> {
                                    var stamper = OfficeStampers.docxStamper(configuration);
                                    stamper.stamp(templateStream, item.context, OutputStream.nullOutputStream());
                                }
                                case POWERPOINT -> {
                                    var stamper = ExperimentalStampers.pptxStamper();
                                    stamper.stamp(templateStream, item.context, OutputStream.nullOutputStream());
                                }
                            }
                            results.add(new RunResult(item.name, "ok", null, null));
                        }
                        else {
                            var out = computeOutputPath(outputPath, item.name, ext);
                            try (var os = createOutputStream(out)) {
                                switch (ext) {
                                    case WORD -> {
                                        var stamper = OfficeStampers.docxStamper();
                                        stamper.stamp(templateStream, item.context, os);
                                    }
                                    case POWERPOINT -> {
                                        var stamper = ExperimentalStampers.pptxStamper();
                                        stamper.stamp(templateStream, item.context, os);
                                    }
                                }
                            }
                            results.add(new RunResult(item.name, "ok", out.toString(), null));
                        }
                    } catch (Exception ex) {
                        emit("ERROR", "Item failed", Map.of("name", item.name, "error", ex.getMessage()), lf);
                        results.add(new RunResult(item.name, "error", null, ex.getMessage()));
                        // Continue with next item; overall exit code should be non-zero if any failed
                    }
                }
                var anyError = results.stream()
                                      .anyMatch(r -> "error".equals(r.status));
                if (dryRun) emit("INFO",
                        "Validation completed (dry-run)",
                        Map.of("items", results.size(), "errors", anyError),
                        lf);
                else emit("INFO", "Stamping completed", Map.of("items", results.size(), "errors", anyError), lf);
                writeReport(results);
                if (anyError) throw new OfficeStamperException("One or more items failed");
                return;
            }

            // Single context path
            final var context = extractContextNew(dataPath);
            try (var templateStream = extractTemplateNew(templatePath)) {
                if (dryRun) {
                    // Validate: fail on unresolved placeholders but do not write any file
                    var configuration = OfficeStamperConfigurations.standard()
                                                                   .setExceptionResolver(pro.verron.officestamper.preset.ExceptionResolvers.throwing());
                    switch (ext) {
                        case WORD -> {
                            var stamper = OfficeStampers.docxStamper(configuration);
                            stamper.stamp(templateStream, context, OutputStream.nullOutputStream());
                        }
                        case POWERPOINT -> {
                            var stamper = ExperimentalStampers.pptxStamper(); // no config variant exposed for PPTX yet
                            stamper.stamp(templateStream, context, OutputStream.nullOutputStream());
                        }
                    }
                    emit("INFO", "Validation successful (dry-run)", null, lf);
                    writeReport("ok", null);
                    return;
                }

                // Real stamping (single file)
                try (var outputStream = createOutputStream(Path.of(outputPath))) {
                    switch (ext) {
                        case WORD -> {
                            var stamper = OfficeStampers.docxStamper();
                            stamper.stamp(templateStream, context, outputStream);
                        }
                        case POWERPOINT -> {
                            var stamper = ExperimentalStampers.pptxStamper();
                            stamper.stamp(templateStream, context, outputStream);
                        }
                    }
                }
            }

            emit("INFO", "Stamping completed", Map.of("output", outputPath), lf);
            writeReport("ok", null);
        } catch (Exception e) {
            emit("ERROR",
                    e.getMessage(),
                    Map.of("exception",
                            e.getClass()
                             .getSimpleName()),
                    lf);
            writeReport("error", e.getMessage());
            // Re-throw to ensure non-zero exit code from picocli
            throw (e instanceof RuntimeException re) ? re : new OfficeStamperException(e);
        }
    }

    private Object extractContextNew(String model) {
        if (model == null || model.isBlank()) {
            // allowed only when templatePath == diagnostic; caller validated earlier
            return Diagnostic.context();
        }
        if ("diagnostic".equals(model)) return Diagnostic.context();
        var path = Path.of(model);
        if (Files.isDirectory(path)) return contextualiseDirectory(path);
        return contextualise(path);
    }

    private InputStream extractTemplateNew(String template) {
        if ("diagnostic".equals(template)) return Diagnostic.template();
        return streamFile(Path.of(template));
    }

    private OutputStream createOutputStream(Path path) {
        try {
            var parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            return newOutputStream(path);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private java.util.List<Item> buildItemsFromDataDirectory(Path dir) {
        try (var stream = Files.list(dir)) {
            var entries = stream.sorted()
                                .toList();
            var items = new java.util.ArrayList<Item>();
            for (var entry : entries) {
                if (Files.isRegularFile(entry) && isSupportedDataFile(entry)) {
                    items.add(new Item(baseName(entry), contextualise(entry)));
                }
                else if (Files.isDirectory(entry)) {
                    items.add(new Item(entry.getFileName()
                                            .toString(), contextualiseDirectoryRecursive(entry)));
                }
            }
            return java.util.List.copyOf(items);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Map<String, Object> contextualiseDirectoryRecursive(Path dir) {
        try (var stream = Files.walk(dir)) {
            var files = stream.filter(Files::isRegularFile)
                              .filter(Main::isSupportedDataFile)
                              .sorted()
                              .toList();
            var map = new LinkedHashMap<String, Object>();
            for (var f : files) {
                map.put(baseName(f), contextualise(f));
            }
            return map;
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Path computeOutputPath(String output, String itemName, TemplateKind ext) {
        var desiredExt = (ext == TemplateKind.WORD) ? ".docx" : ".pptx";
        var out = Path.of(output);
        // If output is an existing directory, place <itemName><ext> inside it
        if (Files.exists(out) && Files.isDirectory(out)) {
            return out.resolve(itemName + desiredExt);
        }
        var fn = out.getFileName() == null
                ? output
                : out.getFileName()
                     .toString();
        var dot = fn.lastIndexOf('.');
        if (dot > 0) {
            var base = fn.substring(0, dot);
            var extPart = fn.substring(dot);
            // Normalize to template extension
            var finalExt = desiredExt;
            var newName = base + "-" + itemName + finalExt;
            var parent = out.getParent();
            return parent == null ? Path.of(newName) : parent.resolve(newName);
        }
        else {
            // Treat as directory path (may or may not exist)
            return out.resolve(itemName + desiredExt);
        }
    }

    private Object contextualise(Path path) {
        var name = path.getFileName()
                       .toString()
                       .toLowerCase();
        if (name.endsWith(".csv")) return processCsv(path);
        if (name.endsWith(".properties")) return processProperties(path);
        if (name.endsWith(".html") || name.endsWith(".xml")) return processXmlOrHtml(path);
        if (name.endsWith(".json")) return processJson(path);
        if (name.endsWith(".yaml") || name.endsWith(".yml")) return processYaml(path);
        if (name.endsWith(".xlsx")) return processExcel(path);
        throw new OfficeStamperException("Unsupported file type: " + path);
    }

    private Map<String, Object> contextualiseDirectory(Path dir) {
        try (var stream = Files.list(dir)) {
            var files = stream.filter(Files::isRegularFile)
                              .filter(p -> {
                                  var n = p.getFileName()
                                           .toString()
                                           .toLowerCase();
                                  return n.endsWith(".csv") || n.endsWith(".properties") || n.endsWith(".html") ||
                                         n.endsWith(".xml") || n.endsWith(".json") || n.endsWith(".yaml") ||
                                         n.endsWith(".yml") || n.endsWith(".xlsx");
                              })
                              .sorted()
                              .toList();
            var map = new LinkedHashMap<String, Object>();
            for (var f : files) {
                var base = f.getFileName()
                            .toString();
                var idx = base.lastIndexOf('.');
                if (idx > 0) base = base.substring(0, idx);
                map.put(base, contextualise(f));
            }
            return map;
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
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

    private Object processYaml(Path path) {
        try {
            // Lazy YAML support using Jackson if available on classpath; else, provide a clear error.
            var mapperClass = Class.forName("com.fasterxml.jackson.dataformat.yaml.YAMLFactory");
            var mapper = new ObjectMapper((com.fasterxml.jackson.core.JsonFactory) mapperClass.getDeclaredConstructor()
                                                                                              .newInstance());
            TypeReference<LinkedHashMap<String, Object>> typeRef = new TypeReference<>() {};
            return mapper.readValue(Files.newInputStream(path), typeRef);
        } catch (ClassNotFoundException e) {
            throw new OfficeStamperException("YAML support requires 'jackson-dataformat-yaml' on the classpath");
        } catch (Exception e) {
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

    private TemplateKind templateKind(String input) {
        if ("diagnostic".equals(input)) return TemplateKind.WORD;
        var lower = input.toLowerCase();
        if (lower.endsWith(".docx")) return TemplateKind.WORD;
        if (lower.endsWith(".pptx")) return TemplateKind.POWERPOINT;
        throw new OfficeStamperException("Unsupported template type (expected .docx or .pptx): " + input);
    }

    private void writeReport(String status, String errorMessage) {
        if (reportPath == null || reportPath.isBlank()) return;
        var report = new LinkedHashMap<String, Object>();
        report.put("status", status);
        report.put("template", templatePath);
        report.put("data", dataPath == null ? "<none>" : dataPath);
        report.put("output", outputPath);
        report.put("dryRun", dryRun);
        report.put("timestamp",
                java.time.OffsetDateTime.now()
                                        .toString());
        if (errorMessage != null) report.put("error", errorMessage);
        try {
            var mapper = new ObjectMapper();
            try (var os = createOutputStream(Path.of(reportPath))) {
                mapper.writeValue(os, report);
            }
        } catch (Exception e) {
            // Best-effort: do not fail the run because report writing failed
            logger.log(Level.WARNING, "Failed to write report: " + e.getMessage(), e);
        }
    }

    private void writeReport(java.util.List<RunResult> results) {
        if (reportPath == null || reportPath.isBlank()) return;
        var report = new LinkedHashMap<String, Object>();
        var anyError = results.stream()
                              .anyMatch(r -> "error".equals(r.status));
        report.put("status", anyError ? "error" : "ok");
        report.put("template", templatePath);
        report.put("data", dataPath);
        report.put("dryRun", dryRun);
        report.put("timestamp",
                java.time.OffsetDateTime.now()
                                        .toString());
        var items = new java.util.ArrayList<Map<String, Object>>();
        for (var r : results) {
            var it = new LinkedHashMap<String, Object>();
            it.put("name", r.name);
            it.put("status", r.status);
            if (r.output != null) it.put("output", r.output);
            if (r.error != null) it.put("error", r.error);
            items.add(it);
        }
        report.put("items", items);
        try {
            var mapper = new ObjectMapper();
            try (var os = createOutputStream(Path.of(reportPath))) {
                mapper.writeValue(os, report);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to write report: " + e.getMessage(), e);
        }
    }

    // Minimal structured logging when --log-format=json
    private void emit(String level, String message, Map<String, Object> fields, String lf) {
        if (!"json".equals(lf)) {
            // Human logs via java.util.logging
            var lvl = switch (level) {
                case "ERROR" -> Level.SEVERE;
                case "WARN" -> Level.WARNING;
                default -> Level.INFO;
            };
            if (fields == null || fields.isEmpty()) logger.log(lvl, message);
            else logger.log(lvl, message + " | " + fields);
            return;
        }
        try {
            var map = new LinkedHashMap<String, Object>();
            map.put("ts",
                    java.time.OffsetDateTime.now()
                                            .toString());
            map.put("level", level.toLowerCase());
            map.put("msg", message);
            if (fields != null && !fields.isEmpty()) map.put("fields", fields);
            var json = new ObjectMapper().writeValueAsString(map);
            System.out.println(json);
        } catch (Exception ignored) {
            System.out.println("{\"level\":\"error\",\"msg\":\"failed to emit json log\"}");
        }
    }

    private enum TemplateKind {
        WORD,
        POWERPOINT
    }

    private record Item(String name, Object context) {}

    /**
     * @param status  ok | error
     * @param output  nullable
     * @param error  nullable */
    private record RunResult(String name, String status, String output, String error) {
    }
}
