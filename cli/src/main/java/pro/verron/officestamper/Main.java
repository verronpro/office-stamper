package pro.verron.officestamper;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.excel.ExcelContext;
import pro.verron.officestamper.excel.ExcelMergeStrategy;
import pro.verron.officestamper.experimental.ExperimentalStampers;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;
import pro.verron.officestamper.preset.OfficeStampers;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.*;
import java.util.stream.IntStream;

import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.time.OffsetDateTime.now;
import static java.util.stream.Collectors.toMap;

/// Main class for the CLI.
@Command(name = "officestamper",
         mixinStandardHelpOptions = true,
         description = "Office Stamper CLI tool",
         subcommands = {Preview.class, ReportView.class}) public class Main
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    @Option(names = {"-t", "--template"},
            defaultValue = "diagnostic",
            description = "Template file path (.docx|.pptx), or a keyword "
                          + "(diagnostic) for a packaged sample template") private String templatePath;
    @Option(names = {"-d", "--data"},
            defaultValue = "diagnostic",
            description = "Data input: file (json|yaml|yml|properties|csv"
                          + "|xlsx|xml|html), a directory, or 'diagnostic'") private String dataPath;
    @Option(names = {"-o", "--output"},
            defaultValue = "output.docx",
            description = "Output file path") private String outputPath;
    @Option(names = {"--dry-run"},
            description = "Validate template + data and variables, but do not"
                          + " produce the output file") private boolean dryRun;
    @Option(names = {"--run-report"},
            defaultValue = "run-report.json",
            description = "Optional JSON report file path with run metadata "
                          + "and validation results") private String reportPath;
    @Option(names = {"--log-format"},
            defaultValue = "human",
            description = "Logging format: 'human' (default) or 'json' "
                          + "(structured logs to stdout)") private String logFormat;

    @Option(names = {"--excel-merge-strategy"},
            defaultValue = "MAP",
            description = "Excel merge strategy: MAP (default, each sheet is "
                          + "a key) or JOIN (inner join sheets)") private ExcelMergeStrategy excelMergeStrategy;
    @Option(names = {"--excel-join-key"},
            description = "Key to use for joining Excel sheets (used with "
                          + "JOIN strategy)") private String excelJoinKey;

    @Option(names = {"--bind-env"},
            description = "Expose environment variables in the SpEL context "
                          + "as 'env'") private boolean bindEnv;

    @Option(names = {"--watch"},
            description = "Watch template and data files for changes and "
                          + "re-run stamping automatically") private boolean watch;

    @Option(names = {"--report", "--traceability-report"},
            defaultValue = "trace-report.json",
            description = "Optional JSON traceability report file path with "
                          + "every placeholder resolution") private String traceabilityReportPath;

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
        var extension = extension(p.getFileName());
        return extension.equals("csv") || //
               extension.equals("properties") || //
               extension.equals("html") || //
               extension.equals("xml") || //
               extension.equals("json") || //
               extension.equals("yaml") || //
               extension.equals("yml") || //
               extension.equals("xlsx");
    }

    private static String baseName(Path f) {
        var fileName = f.getFileName();
        var base = fileName.toString();
        var idx = base.lastIndexOf('.');
        if (idx > 0) base = base.substring(0, idx);
        return base;
    }

    private static String extension(Path f) {
        var fileName = f.getFileName();
        var base = fileName.toString();
        var idx = base.lastIndexOf('.');
        if (idx > 0) base = base.substring(idx + 1);
        return base;
    }

    private static Map<String, String> mapRowToHeaders(
            String[] row,
            String[] headers
    ) {
        return IntStream.range(0, headers.length)
                        .boxed()
                        .collect(toMap(i -> headers[i],
                                i -> row[i],
                                (_, b) -> b,
                                LinkedHashMap::new));
    }

    @Override
    public void run() {
        if (watch) runWatch();
        else runOnce();
    }

    private Map<String, Object> contextualiseDirectory(Path dir) {
        try (var stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile)
                         .filter(Main::isSupportedDataFile)
                         .collect(toMap(Main::baseName,
                                 this::contextualise,
                                 (_, b) -> b,
                                 TreeMap::new));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object extractContextNew(String model) {
        if ("diagnostic".equals(model)) return Diagnostic.context();
        var path = Path.of(model);
        return Files.isDirectory(path)
                ? contextualiseDirectory(path)
                : contextualise(path);
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
            var items = new ArrayList<Item>();
            for (var entry : entries) {
                if (Files.isRegularFile(entry) && isSupportedDataFile(entry)) {
                    items.add(new Item(baseName(entry), contextualise(entry)));
                }
                else if (Files.isDirectory(entry)) {
                    items.add(new Item(baseName(entry),
                            contextualiseDirectoryRecursive(entry)));
                }
            }
            return List.copyOf(items);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Map<String, Object> contextualiseDirectoryRecursive(Path dir) {
        try (var stream = Files.walk(dir)) {
            return stream.filter(Files::isRegularFile)
                         .filter(Main::isSupportedDataFile)
                         .collect(toMap(Main::baseName,
                                 this::contextualise,
                                 (_, b) -> b,
                                 TreeMap::new));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Path computeOutputPath(
            String output,
            String itemName,
            TemplateKind ext
    ) {
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
            // Normalize to template extension
            var newName = base + "-" + itemName + desiredExt;
            var parent = out.getParent();
            return parent == null ? Path.of(newName) : parent.resolve(newName);
        }
        else {
            // Treat as directory path (may or may not exist)
            return out.resolve(itemName + desiredExt);
        }
    }

    private Object contextualise(Path path) {
        var extension = extension(path);
        return switch (extension) {
            case "csv" -> processCsv(path);
            case "properties" -> processProperties(path);
            case "html", "xml" -> processXmlOrHtml(path);
            case "json" -> processJson(path);
            case "yaml", "yml" -> processYaml(path);
            case "xlsx" -> processExcel(path);
            default -> throw new OfficeStamperException(
                    "Unsupported file type: " + path);
        };
    }

    private void runWatch() {
        var lf = getLogFormat();
        emit("INFO", "Watch mode enabled", null, lf);
        try {
            runOnce();
        } catch (Exception e) {
            var error = Map.of("error", String.valueOf(e.getMessage()));
            emit("ERROR", "Initial run failed", error, lf);
        }

        try (
                var watchService = FileSystems.getDefault()
                                              .newWatchService()
        ) {
            var templateFile = "diagnostic".equals(templatePath)
                    ? null
                    : Path.of(templatePath)
                          .toAbsolutePath();
            var dataFile = "diagnostic".equals(dataPath)
                    ? null
                    : Path.of(dataPath)
                          .toAbsolutePath();

            var pathsToWatch = new HashSet<Path>();
            if (templateFile != null) pathsToWatch.add(templateFile);
            if (dataFile != null) pathsToWatch.add(dataFile);

            var keys = new HashMap<WatchKey, Path>();
            for (var p : pathsToWatch) {
                var dir = Files.isDirectory(p) ? p : p.getParent();
                if (dir != null && Files.exists(dir)) {
                    var key = dir.register(watchService, ENTRY_MODIFY);
                    keys.put(key, dir);
                }
            }

            while (true) {
                var key = watchService.take();
                var dir = keys.get(key);
                for (var event : key.pollEvents()) {
                    if (event.kind() == OVERFLOW) continue;
                    var context = (Path) event.context();
                    var resolved = dir.resolve(context);

                    var relevant = false;
                    for (var p : pathsToWatch) {
                        if (resolved.equals(p) || (Files.isDirectory(p)
                                                   && resolved.startsWith(p))) {
                            relevant = true;
                            break;
                        }
                    }

                    if (relevant) {
                        var change = Map.of("file", resolved.toString());
                        emit("INFO",
                                "Change detected, re-stamping...",
                                change,
                                lf);
                        try {
                            runOnce();
                        } catch (Exception e) {
                            var errorMessage = String.valueOf(e.getMessage());
                            var error = Map.of("error", errorMessage);
                            emit("ERROR", "Re-stamping failed", error, lf);
                        }
                    }
                }
                if (!key.reset()) break;
            }
        } catch (Exception e) {
            var errorMessage = String.valueOf(e.getMessage());
            var error = Map.of("error", errorMessage);
            emit("ERROR", "Watch mode failed", error, lf);
        }
    }

    /// Return a list of objects with the csv properties
    private Object processCsv(Path path) {
        try (
                var inputStream = Files.newInputStream(path);
                var streamReader = new InputStreamReader(inputStream);
                var reader = new CSVReader(streamReader);
        ) {
            var headers = reader.readNext();
            return reader.readAll()
                         .stream()
                         .map(row -> mapRowToHeaders(row, headers))
                         .toList();
        } catch (IOException | CsvException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processProperties(Path path) {
        var properties = new Properties();
        try (var inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
            return properties.entrySet()
                             .stream()
                             .collect(toMap(e -> String.valueOf(e.getKey()),
                                     e -> String.valueOf(e.getValue()),
                                     (a, b) -> b,
                                     LinkedHashMap::new));
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processXmlOrHtml(Path path) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setExpandEntityReferences(false);

            var builder = factory.newDocumentBuilder();
            var document = builder.parse(Files.newInputStream(path));
            return processNode(document.getDocumentElement());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Map<String, Object> processNode(Element element) {
        var result = new LinkedHashMap<String, Object>();
        var children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            var node = children.item(i);
            if (node instanceof Element childElement) {
                var name = childElement.getTagName();
                if (!childElement.hasChildNodes()) {
                    result.put(name, childElement.getTextContent());
                }
                else {
                    var firstChild = childElement.getFirstChild();
                    if (firstChild.getNodeType() == Node.TEXT_NODE) {
                        result.put(name, childElement.getTextContent());
                    }
                    else {
                        result.put(name, processNode(childElement));
                    }
                }
            }
        }
        return result;
    }

    private Object processJson(Path path) {
        try {
            var mapper = SerializationUtils.newMapper();
            var typeRef = new TypeReference<LinkedHashMap<String, Object>>() {};
            return mapper.readValue(Files.newInputStream(path), typeRef);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processYaml(Path path) {
        try {
            // Lazy YAML support using Jackson if available on classpath;
            // else, provide a clear error.
            var yaml = "com.fasterxml.jackson.dataformat.yaml.YAMLFactory";
            var mapperClass = Class.forName(yaml);
            var declaredConstructor = mapperClass.getDeclaredConstructor();
            var jsonFactory = (JsonFactory) declaredConstructor.newInstance();
            var mapper = new ObjectMapper(jsonFactory);
            var typeRef = new TypeReference<LinkedHashMap<String, Object>>() {};
            return mapper.readValue(Files.newInputStream(path), typeRef);
        } catch (ClassNotFoundException e) {
            var msg = "YAML support requires 'jackson-dataformat-yaml' on the"
                      + " classpath";
            throw new OfficeStamperException(msg);
        } catch (Exception e) {
            throw new OfficeStamperException(e);
        }
    }

    private Object processExcel(Path path) {
        try (var is = Files.newInputStream(path)) {
            var ctx = ExcelContext.from(is);
            if (excelMergeStrategy == ExcelMergeStrategy.JOIN) {
                return ctx.joinAllSheets(excelJoinKey);
            }
            return ctx;
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private TemplateKind templateKind(String input) {
        if ("diagnostic".equals(input)) return TemplateKind.WORD;
        var lower = input.toLowerCase();
        if (lower.endsWith(".docx")) return TemplateKind.WORD;
        if (lower.endsWith(".pptx")) return TemplateKind.POWERPOINT;
        var msg = "Unsupported template type (expected .docx or .pptx): %s";
        throw new OfficeStamperException(msg.formatted(input));
    }

    private String getLogFormat() {
        return logFormat.trim()
                        .toLowerCase();
    }

    private void runOnce() {
        var traceabilityReport = new TraceabilityReport(now(),
                templatePath,
                dataPath);
        // Normalize log format
        var lf = getLogFormat();

        if (templatePath.isBlank()) {
            emit("ERROR", "Missing required --template path", null, lf);
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "--template is required");
        }
        if (dataPath.isBlank() && !"diagnostic".equals(templatePath)) {
            emit("ERROR",
                    "Missing required --data when not using diagnostic "
                    + "template",
                    null,
                    lf);
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "--data is required when template != diagnostic");
        }

        emit("INFO",
                "Start",
                Map.of("template",
                        templatePath,
                        "data",
                        dataPath,
                        "output",
                        outputPath,
                        "dryRun",
                        dryRun),
                lf);

        try {
            var ext = templateKind(templatePath);
            // Folder semantics: each top-level file is its own context and
            // yields one output;
            // each top-level subfolder merges its files (recursively) into a
            // bigger context and yields one output.
            if (!dataPath.isBlank() && Files.isDirectory(Path.of(dataPath))) {
                var items = buildItemsFromDataDirectory(Path.of(dataPath));
                var results = new ArrayList<RunResult>(items.size());
                int idx = 0;
                for (var item : items) {
                    idx++;
                    emit("INFO",
                            "Processing item",
                            Map.of("index",
                                    idx,
                                    "name",
                                    item.name,
                                    "total",
                                    items.size()),
                            lf);
                    try (
                            var templateStream =
                                    extractTemplateNew(templatePath)
                    ) {
                        var context = wrapContext(item.context);
                        var configuration =
                                OfficeStamperConfigurations.standard();
                        configuration.setTraceabilityReporter(traceabilityReport);
                        if (dryRun) {
                            configuration.setExceptionResolver(
                                    ExceptionResolvers.throwing());
                            switch (ext) {
                                case WORD -> {
                                    var stamper = OfficeStampers.docxStamper(
                                            configuration);
                                    stamper.stamp(templateStream,
                                            context,
                                            OutputStream.nullOutputStream());
                                }
                                case POWERPOINT -> {
                                    var stamper =
                                            ExperimentalStampers.pptxStamper();
                                    stamper.stamp(templateStream,
                                            context,
                                            OutputStream.nullOutputStream());
                                }
                            }
                            results.add(new RunResult(item.name,
                                    "ok",
                                    null,
                                    null));
                        }
                        else {
                            var out = computeOutputPath(outputPath,
                                    item.name,
                                    ext);
                            try (var os = createOutputStream(out)) {
                                switch (ext) {
                                    case WORD -> {
                                        var stamper =
                                                OfficeStampers.docxStamper(
                                                configuration);
                                        stamper.stamp(templateStream,
                                                context,
                                                os);
                                    }
                                    case POWERPOINT -> {
                                        var stamper =
                                                ExperimentalStampers.pptxStamper();
                                        stamper.stamp(templateStream,
                                                context,
                                                os);
                                    }
                                }
                            }
                            results.add(new RunResult(item.name,
                                    "ok",
                                    out.toString(),
                                    null));
                        }
                    } catch (Exception ex) {
                        emit("ERROR",
                                "Item failed",
                                Map.of("name",
                                        item.name,
                                        "error",
                                        ex.getMessage()),
                                lf);
                        results.add(new RunResult(item.name,
                                "error",
                                null,
                                ex.getMessage()));
                        // Continue with next item; overall exit code should
                        // be non-zero if any failed
                    }
                }
                var anyError = results.stream()
                                      .anyMatch(r -> "error".equals(r.status));
                if (dryRun) emit("INFO",
                        "Validation completed (dry-run)",
                        Map.of("items", results.size(), "errors", anyError),
                        lf);
                else emit("INFO",
                        "Stamping completed",
                        Map.of("items", results.size(), "errors", anyError),
                        lf);
                writeReport(results);
                if (anyError) throw new OfficeStamperException(
                        "One or more items " + "failed");
                return;
            }

            // Single context path
            final var context = wrapContext(extractContextNew(dataPath));
            try (var templateStream = extractTemplateNew(templatePath)) {
                var configuration = OfficeStamperConfigurations.standard();
                configuration.setTraceabilityReporter(traceabilityReport);
                if (dryRun) {
                    // Validate: fail on unresolved placeholders but do not
                    // write any file
                    configuration.setExceptionResolver(ExceptionResolvers.throwing());
                    switch (ext) {
                        case WORD -> {
                            var stamper = OfficeStampers.docxStamper(
                                    configuration);
                            stamper.stamp(templateStream,
                                    context,
                                    OutputStream.nullOutputStream());
                        }
                        case POWERPOINT -> {
                            var stamper = ExperimentalStampers.pptxStamper(); // no config variant exposed for PPTX yet
                            stamper.stamp(templateStream,
                                    context,
                                    OutputStream.nullOutputStream());
                        }
                    }
                    emit("INFO", "Validation successful (dry-run)", null, lf);
                    writeReport("ok", null);
                    writeTraceabilityReport(traceabilityReport,
                            Path.of(traceabilityReportPath));
                    return;
                }

                // Real stamping (single file)
                try (
                        var outputStream =
                                createOutputStream(Path.of(outputPath))
                ) {
                    switch (ext) {
                        case WORD -> {
                            var stamper = OfficeStampers.docxStamper(
                                    configuration);
                            stamper.stamp(templateStream,
                                    context,
                                    outputStream);
                        }
                        case POWERPOINT -> {
                            var stamper = ExperimentalStampers.pptxStamper(); // no config variant exposed for PPTX yet
                            stamper.stamp(templateStream,
                                    context,
                                    outputStream);
                        }
                    }
                }
            }

            emit("INFO",
                    "Stamping completed",
                    Map.of("output", outputPath),
                    lf);
            writeReport("ok", null);
            writeTraceabilityReport(traceabilityReport,
                    Path.of(traceabilityReportPath));
        } catch (Exception e) {
            emit("ERROR",
                    e.getMessage(),
                    Map.of("exception",
                            e.getClass()
                             .getSimpleName()),
                    lf);
            writeReport("error", e.getMessage());
            // Re-throw to ensure non-zero exit code from picocli
            throw (e instanceof RuntimeException re)
                    ? re
                    : new OfficeStamperException(e);
        }
    }

    // Minimal structured logging when --log-format=json
    private void emit(
            String level,
            String message,
            @Nullable Map<String, ?> fields,
            String lf
    ) {
        if (!"json".equals(lf)) {
            // Human logs via java.util.logging
            var lvl = switch (level) {
                case "ERROR" -> Level.ERROR;
                case "WARN" -> Level.WARN;
                default -> Level.INFO;
            };
            logger.atLevel(lvl)
                  .log(fields == null || fields.isEmpty()
                          ? message
                          : message + " | " + fields);
            return;
        }
        try {
            var map = new LinkedHashMap<String, Object>();
            map.put("ts", now().toString());
            map.put("level", level.toLowerCase());
            map.put("msg", message);
            if (fields != null && !fields.isEmpty()) map.put("fields", fields);
            var json = new ObjectMapper().writeValueAsString(map);
            System.out.println(json);
        } catch (Exception ignored) {
            System.out.println(
                    "{\"level\":\"error\",\"msg\":\"failed to emit json "
                    + "log\"}");
        }
    }

    private void writeReport(java.util.List<RunResult> results) {
        if (reportPath.isBlank()) return;
        var report = new LinkedHashMap<String, Object>();
        var anyError = results.stream()
                              .anyMatch(r -> "error".equals(r.status));
        report.put("status", anyError ? "error" : "ok");
        report.put("template", templatePath);
        report.put("data", dataPath);
        report.put("dryRun", dryRun);
        report.put("timestamp", now().toString());
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
            var mapper = SerializationUtils.newMapper();
            try (var os = createOutputStream(Path.of(reportPath))) {
                mapper.writeValue(os, report);
            }
        } catch (Exception e) {
            logger.atWarn()
                  .setCause(e)
                  .log("Failed to write report: {}", e.getMessage());
        }
    }

    private void writeReport(String status, @Nullable String errorMessage) {
        if (reportPath.isBlank()) return;
        var report = new LinkedHashMap<String, Object>();
        report.put("status", status);
        report.put("template", templatePath);
        report.put("data", dataPath);
        report.put("output", outputPath);
        report.put("dryRun", dryRun);
        report.put("timestamp", now().toString());
        if (errorMessage != null) report.put("error", errorMessage);
        try {
            var mapper = SerializationUtils.newMapper();
            try (var os = createOutputStream(Path.of(reportPath))) {
                mapper.writeValue(os, report);
            }
        } catch (Exception e) {
            // Best-effort: do not fail the run because report writing failed
            logger.atWarn()
                  .setCause(e)
                  .log("Failed to write report: {}", e.getMessage());
        }
    }

    private Object wrapContext(Object context) {
        if (!bindEnv) return context;
        var wrapper = new LinkedHashMap<String, Object>();
        wrapper.put("env", System.getenv());
        if (context instanceof Map<?, ?> map) {
            for (var entry : map.entrySet()) {
                wrapper.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        else {
            wrapper.put("data", context);
        }
        return wrapper;
    }

    private void writeTraceabilityReport(TraceabilityReport report, Path path) {
        try {
            var mapper = SerializationUtils.newMapper();
            try (var os = createOutputStream(path)) {
                var objectWriter = mapper.writerWithDefaultPrettyPrinter();
                objectWriter.writeValue(os, report);
            }
        } catch (IOException e) {
            logger.warn("Could not write traceability report", e);
        }
    }

    private enum TemplateKind {
        WORD,
        POWERPOINT
    }

    private record Item(String name, Object context) {}

    /**
     * @param status ok | error
     * @param output nullable
     * @param error  nullable
     */
    private record RunResult(
            String name,
            String status,
            @Nullable String output,
            @Nullable String error
    ) {}
}
