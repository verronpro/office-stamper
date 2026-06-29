package pro.verron.officestamper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import pro.verron.asciidoc.compiler.AsciiDocCompiler;
import pro.verron.asciidoc.core.AsciiDocModel;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.writeString;
import static pro.verron.asciidoc.compiler.AsciiDocCompiler.saveSvgAsImage;

/// Subcommand that generates a preview image from an AsciiDoc file.
@Command(
        name = "preview", description = "Generate a preview image from an AsciiDoc" + " file"
)
public class Preview implements Runnable {

    @Option(
            names = {"-i", "--input"}, required = true, description = "Input AsciiDoc file"
    )
    private Path input;

    @Option(
            names = {"-o", "--output"}, defaultValue = "preview.png", description = "Output file (PNG or SVG)"
    )
    private Path output;

    @Option(
            names = "--theme", defaultValue = "word", description = "Theme: word, gdocs, libre"
    )
    private String theme;

    @Option(
            names = "--dpi", defaultValue = "96", description = "DPI for PNG output"
    )
    private int dpi;

    @Option(
            names = "--format", description = "Output format: png, svg (auto-detected if omitted)"
    )
    private String format;

    /// Default constructor.
    public Preview() {
    }

    @Override
    public void run() {
        try {
            String asciidoc = Files.readString(input);
            var model = AsciiDocCompiler.toModel(asciidoc);

            // Inject theme attribute into model
            var attributes = new java.util.HashMap<>(model.getAttributes());
            attributes.put("theme", theme);
            model = AsciiDocModel.of(attributes, model.getBlocks());

            String formatToUse;
            if (format != null) formatToUse = format;
            else formatToUse = output.endsWith(".svg") ? "svg" : "png";

            var svg = AsciiDocCompiler.toSvg(model);

            switch (formatToUse) {
                case "svg" -> writeString(output, svg);
                case "png" -> saveSvgAsImage(svg, output, dpi, Color.WHITE);
                default -> throw new IllegalArgumentException("Unsupported format: " + formatToUse);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate preview", e);
        }
    }
}
