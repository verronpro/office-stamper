package pro.verron.officestamper;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.ExceptionResolvers;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.time.OffsetDateTime.now;
import static pro.verron.officestamper.TemplateKind.WORD;

@Command(name = "stamp")
public class Stamp implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Option(
            names = {"-t", "--template"},
            required = true,
            description = "Template file path (.docx|.pptx), or a keyword (diagnostic) for a packaged sample template"
    )
    private String templatePath;

    @Option(
            names = {"-d", "--data"},
            required = true,
            description = "Data input: file (json|yaml|yml|properties|csv|xlsx|xml|html), a directory, or 'diagnostic'"
    )
    private String dataPath;

    @Option(
            names = {"-o", "--output"}, defaultValue = "output.docx", required = false, description = "Output file path"
    )
    private String outputPath;

    @Option(
            names = {"--dry-run"},
            required = false,
            description = "Validate template + data and variables, but do not produce the output file"
    )
    private boolean dryRun;

    @Option(
            names = {"--run-report"},
            required = false,
            description = "Optional JSON report file path with run metadata and validation results"
    )
    private boolean printRunReport;

    @Option(
            names = {"--run-report-path"},
            required = false,
            defaultValue = "run-report.json",
            description = "Optional JSON report file path with run metadata and validation results"
    )
    private String runReportPath;

    @ArgGroup(heading = "Excel options:", exclusive = false)
    private ExcelStrategy excelStrategy;

    @Option(
            names = {"--trace-report"},
            required = false,
            description = "Optional JSON traceability report file path with every placeholder resolution"
    )
    private boolean printTraceReport;

    @Option(
            names = {"--trace-report-path"},
            defaultValue = "trace-report.json",
            description = "Optional JSON traceability report file path with every placeholder resolution"
    )
    private String traceReportPath;

    @Option(
            names = {"--watch"},
            description = "Watch template and data files for changes and re-run stamping automatically"
    )
    private boolean watch;

    @Option(
            names = {"--log-format"},
            required = false,
            defaultValue = "human",
            description = "Logging format: 'human' (default) or 'json' (structured logs to stdout)"
    )
    private String logFormat;


    @Option(names = {"--bind-env"}, description = "Expose environment variables in the SpEL context as 'env'")
    private boolean bindEnv;

    private static void writeReport(Object reportData, Path path) {
        try {
            var mapper = SerializationUtils.newMapper();
            var writer = mapper.writerWithDefaultPrettyPrinter();
            try (var os = PathUtils.createOutputStream(path)) {
                writer.writeValue(os, reportData);
            }
        } catch (Exception e) {
            // Best-effort: do not fail the run because report writing failed
            logger.atWarn().setCause(e).log("Failed to write report: {}", e.getMessage());
        }
    }

    private static Object addEnv(Object context) {
        if (context instanceof Map<?, ?> map) {
            var wrapper = new LinkedHashMap<String, Object>();
            wrapper.put("env", System.getenv());
            for (var entry : map.entrySet()) {
                wrapper.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return wrapper;
        } else {
            var wrapper = new LinkedHashMap<String, Object>();
            wrapper.put("env", System.getenv());
            wrapper.put("data", context);
            return wrapper;
        }
    }

    @Override
    public void run() {
        if (watch) runWatch();
        else runOnce();
    }

    private void runWatch() {
        var lf = getLogFormat();
        lf.emit("INFO", "Watch mode enabled", null);
        try {
            runOnce();
        } catch (Exception e) {
            var error = Map.of("error", String.valueOf(e.getMessage()));
            lf.emit("ERROR", "Initial run failed", error);
        }

        try (var watchService = FileSystems.getDefault().newWatchService()) {
            var templateFile = "diagnostic".equals(templatePath) ? null : Path.of(templatePath).toAbsolutePath();
            var dataFile = "diagnostic".equals(dataPath) ? null : Path.of(dataPath).toAbsolutePath();

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
                        if (resolved.equals(p) || (Files.isDirectory(p) && resolved.startsWith(p))) {
                            relevant = true;
                            break;
                        }
                    }

                    if (relevant) {
                        var change = Map.of("file", resolved.toString());
                        lf.emit("INFO", "Change detected, re-stamping...", change);
                        try {
                            runOnce();
                        } catch (Exception e) {
                            var errorMessage = String.valueOf(e.getMessage());
                            var error = Map.of("error", errorMessage);
                            lf.emit("ERROR", "Re-stamping failed", error);
                        }
                    }
                }
                if (!key.reset()) break;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            var errorMessage = String.valueOf(e.getMessage());
            var error = Map.of("error", errorMessage);
            lf.emit("ERROR", "Watch mode failed", error);
        }
    }

    private void runOnce() {
        var traceabilityReport = new TraceabilityReport(now(), templatePath, dataPath);
        // Normalize log format
        var lf = getLogFormat();

        if (templatePath.isBlank()) {
            lf.emit("ERROR", "Missing required --template path", null);
            throw new CommandLine.ParameterException(new CommandLine(this), "--template is required");
        }
        if (dataPath.isBlank() && !"diagnostic".equals(templatePath)) {
            lf.emit("ERROR", "Missing required --data when not using diagnostic " + "template", null);
            throw new CommandLine.ParameterException(new CommandLine(this),
                    "--data is required when template != diagnostic"
            );
        }

        lf.emit("INFO",
                "Start",
                Map.of("template", templatePath, "data", dataPath, "output", outputPath, "dryRun", dryRun)
        );

        try {
            var ext = "diagnostic".equals(templatePath) ? WORD : TemplateKind.templateKind(templatePath);
            // Folder semantics: each top-level file is its own context and
            // yields one output;
            // each top-level subfolder merges its files (recursively) into a
            // bigger context and yields one output.
            if (!dataPath.isBlank() && Files.isDirectory(Path.of(dataPath))) {
                var items = Contextualizer.contextualizeDir(Path.of(dataPath), excelStrategy);
                var results = new ArrayList<RunResult>(items.size());
                int idx = 0;
                for (var item : items) {
                    idx++;
                    lf.emit("INFO",
                            "Processing item",
                            Map.of("index", idx, "name", item.name(), "total", items.size())
                    );

                    var outputFilePath = PathUtils.computeOutputPath(outputPath,
                            item.name(),
                            (ext == WORD) ? ".docx" : ".pptx"
                    );
                    try (var templateStream = "diagnostic".equals(templatePath) ? Diagnostic.template() : PathUtils.streamFile(
                            Path.of(templatePath)); var out = dryRun ? OutputStream.nullOutputStream() : PathUtils.createOutputStream(
                            outputFilePath)) {
                        var context = wrapContext(item.context(), bindEnv);
                        var configuration = OfficeStamperConfigurations.standard();
                        configuration.setTraceabilityReporter(traceabilityReport);
                        if (dryRun) {
                            configuration.setExceptionResolver(ExceptionResolvers.throwing());
                            ext.stamp(templateStream, context, configuration, out);
                            results.add(new RunResult(item.name(), "ok", null, null));
                        } else {
                            ext.stamp(templateStream, context, configuration, out);
                            results.add(new RunResult(item.name(), "ok", outputFilePath.toString(), null));
                        }

                    } catch (Exception ex) {
                        lf.emit("ERROR", "Item failed", Map.of("name", item.name(), "error", ex.getMessage()));
                        results.add(new RunResult(item.name(), "error", null, ex.getMessage()));
                        // Continue with next item; overall exit code should
                        // be non-zero if any failed
                    }
                }
                var anyError = results.stream().anyMatch(r -> "error".equals(r.status()));
                if (dryRun) lf.emit("INFO",
                        "Validation completed (dry-run)",
                        Map.of("items", results.size(), "errors", anyError)
                );
                else lf.emit("INFO", "Stamping completed", Map.of("items", results.size(), "errors", anyError));
                if (printRunReport) writeReport(createResultReport(results), Path.of(runReportPath));
                if (anyError) throw new OfficeStamperException("One or more items " + "failed");
                return;
            }

            // Single context path
            final var context = wrapContext("diagnostic".equals(dataPath) ? Diagnostic.context() : Contextualizer.contextualize(
                            excelStrategy,
                            Path.of(dataPath)
                    ), bindEnv
            );
            try (var templateStream = "diagnostic".equals(templatePath) ? Diagnostic.template() : PathUtils.streamFile(
                    Path.of(templatePath))) {
                var configuration = OfficeStamperConfigurations.standard();
                configuration.setTraceabilityReporter(traceabilityReport);
                if (dryRun) {
                    // Validate: fail on unresolved placeholders but do not
                    // write any file
                    configuration.setExceptionResolver(ExceptionResolvers.throwing());
                    ext.stamp(templateStream, context, configuration, OutputStream.nullOutputStream());
                    lf.emit("INFO", "Validation successful (dry-run)", null);
                    Object reportData = createStatusReport("ok", null);
                    if (printRunReport) writeReport(reportData, Path.of(runReportPath));
                    if (printTraceReport) writeReport(traceabilityReport, Path.of(traceReportPath));
                    return;
                }

                // Real stamping (single file)
                try (var outputStream = PathUtils.createOutputStream(Path.of(outputPath))) {
                    ext.stamp(templateStream, context, configuration, outputStream);
                }
            }

            lf.emit("INFO", "Stamping completed", Map.of("output", outputPath));
            Object reportData = createStatusReport("ok", null);
            if (printRunReport) writeReport(reportData, Path.of(runReportPath));
            if (printTraceReport) writeReport(traceabilityReport, Path.of(traceReportPath));
        } catch (Exception e) {
            lf.emit("ERROR", e.getMessage(), Map.of("exception", e.getClass().getSimpleName()));
            Object reportData = createStatusReport("error", e.getMessage());
            if (printRunReport) writeReport(reportData, Path.of(runReportPath));
            // Re-throw to ensure non-zero exit code from picocli
            throw (e instanceof RuntimeException re) ? re : new OfficeStamperException(e);
        }
    }

    private Emitter getLogFormat() {
        var logFormatValue = logFormat.trim().toLowerCase();
        return switch (logFormatValue) {
            case "json" -> new JsonEmitter();
            default -> new LogEmitter();
        };
    }

    private Object wrapContext(Object context, boolean addEnv) {
        return addEnv ? addEnv(context) : context;
    }

    private Object createResultReport(List<RunResult> results) {
        var report = new LinkedHashMap<String, Object>();
        var anyError = results.stream().anyMatch(r -> "error".equals(r.status()));
        report.put("status", anyError ? "error" : "ok");
        report.put("template", templatePath);
        report.put("data", dataPath);
        report.put("dryRun", dryRun);
        report.put("timestamp", now().toString());
        var items = new ArrayList<Map<String, Object>>();
        for (var r : results) {
            var it = new LinkedHashMap<String, Object>();
            it.put("name", r.name());
            it.put("status", r.status());
            if (r.output() != null) it.put("output", r.output());
            if (r.error() != null) it.put("error", r.error());
            items.add(it);
        }
        report.put("items", items);
        return report;
    }

    private Object createStatusReport(String status, @Nullable String errorMessage) {
        var report = new LinkedHashMap<String, Object>();
        report.put("status", status);
        report.put("template", templatePath);
        report.put("data", dataPath);
        report.put("output", outputPath);
        report.put("dryRun", dryRun);
        report.put("timestamp", now().toString());
        if (errorMessage != null) report.put("error", errorMessage);
        return report;
    }
}
