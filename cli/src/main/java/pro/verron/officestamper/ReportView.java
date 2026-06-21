package pro.verron.officestamper;

import com.fasterxml.jackson.core.type.TypeReference;
import picocli.CommandLine;
import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.writeString;

/// Subcommand that generates an HTML viewer for a traceability report.
@CommandLine.Command(name = "report-view",
                     description = "Generate an HTML viewer for a "
                                   + "traceability report")
public class ReportView
        implements Runnable {

    @CommandLine.Option(names = {"-i", "--input"},
                        required = true,
                        description = "JSON traceability report") private Path input;
    @CommandLine.Option(names = {"-o", "--output"},
                        defaultValue = "traceability.html",
                        description = "Output HTML file") private Path output;

    /// Default constructor.
    public ReportView() {}

    @Override
    public void run() {
        try {
            var mapper = SerializationUtils.newMapper();
            var root = mapper.readTree(input.toFile());
            var resolutionsNode = root.get("resolutions");
            if (resolutionsNode == null) {
                // Fallback for old format or if it was just a list
                resolutionsNode = root.isArray() ? root : null;
            }

            if (resolutionsNode == null) throw new OfficeStamperException(
                    "Could not find resolutions in report");

            List<TraceabilityReport.Resolution> resolutions =
                    mapper.convertValue(
                            resolutionsNode,
                            new TypeReference<>() {});
            var html = generateHtml(resolutions);
            writeString(output, html);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    private String generateHtml(List<TraceabilityReport.Resolution> resolutions) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Traceability Report</title><style>");
        sb.append("table { border-collapse: collapse; width: 100%; "
                  + "font-family: sans-serif; }");
        sb.append("th, td { border: 1px solid #ddd; padding: 8px; "
                  + "text-align: left; vertical-align: top; }");
        sb.append("th { background-color: #4CAF50; color: white; }");
        sb.append("tr:nth-child(even) { background-color: #f9f9f9; }");
        sb.append("tr:hover { background-color: #f1f1f1; }");
        sb.append("ul { margin: 0; padding-left: 20px; }");
        sb.append("</style></head><body>");
        sb.append("<h1>Office Stamper Traceability Report</h1>");
        sb.append("<table><tr><th>Expression</th><th>Resolved "
                  + "Value</th><th>Nesting Context</th></tr>");
        for (var res : resolutions) {
            sb.append("<tr>");
            sb.append("<td><code>")
              .append(res.expression())
              .append("</code></td>");
            sb.append("<td>")
              .append(res.value())
              .append("</td>");
            sb.append("<td><ul>");
            for (var ctx : res.contextStack()) {
                sb.append("<li>")
                  .append(ctx)
                  .append("</li>");
            }
            sb.append("</ul></td>");
            sb.append("</tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }
}
