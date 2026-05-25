package pro.verron.officestamper.asciidoc.preview;

import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;
import pro.verron.officestamper.asciidoc.compiler.AsciiDocCompiler;
import pro.verron.officestamper.asciidoc.core.AsciiDocModel;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;

/// AsciiDoc block macro to embed a preview of another AsciiDoc template.
/// Usage: `preview::template.adoc[theme=word,format=png,dpi=192]`
@Name("preview")
public class AsciiDocPreviewBlockMacro
        extends BlockMacroProcessor {

    /// Constructs a new [AsciiDocPreviewBlockMacro].
    public AsciiDocPreviewBlockMacro() {
    }

    /// Constructs a new [AsciiDocPreviewBlockMacro] with the specified macro name.
    ///
    /// @param macroName the name of the macro
    public AsciiDocPreviewBlockMacro(String macroName) {
        super(macroName);
    }

    @Override
    public StructuralNode process(StructuralNode parent, String target, Map<String, Object> attributes) {
        String docDirAttr = (String) parent.getDocument()
                                           .getAttribute("docdir");
        if (docDirAttr == null) docDirAttr = ".";
        Path docDir = Paths.get(docDirAttr);
        Path adocPath = docDir.resolve(target);

        if (!Files.exists(adocPath)) {
            return createBlock(parent, "paragraph", "Preview file not found: " + adocPath.toAbsolutePath());
        }

        String theme = (String) attributes.getOrDefault("theme", "word");
        String format = (String) attributes.getOrDefault("format", "png");
        int dpi = Integer.parseInt((String) attributes.getOrDefault("dpi", "96"));

        try {
            String content = Files.readString(adocPath);
            AsciiDocModel model = AsciiDocCompiler.toModel(content);

            // Re-create model with overridden theme
            Map<String, String> newAttributes = new HashMap<>(model.getAttributes());
            newAttributes.put("theme", theme);
            model = AsciiDocModel.of(model.getBlocks(), newAttributes);

            String baseName = target.contains(".") ? target.substring(0, target.lastIndexOf('.')) : target;
            String fileName = baseName + "-" + theme + "-" + dpi + "." + format;

            String imagesOutDirAttr = (String) parent.getDocument()
                                                     .getAttribute("imagesoutdir");
            Path imagesOutDir;
            if (imagesOutDirAttr != null) {
                imagesOutDir = Paths.get(imagesOutDirAttr);
            }
            else {
                String outDirAttr = (String) parent.getDocument()
                                                   .getAttribute("outdir");
                if (outDirAttr != null) {
                    imagesOutDir = Paths.get(outDirAttr);
                }
                else {
                    imagesOutDir = docDir;
                }
            }

            Path outputPath = imagesOutDir.resolve(fileName);
            Files.createDirectories(imagesOutDir);

            // Caching: check if output exists and is newer than source
            if (!Files.exists(outputPath) || Files.getLastModifiedTime(outputPath)
                                                  .toMillis() < Files.getLastModifiedTime(adocPath)
                                                                     .toMillis()) {
                if ("svg".equalsIgnoreCase(format)) {
                    String svg = AsciiDocCompiler.toSvg(model);
                    Files.writeString(outputPath, svg);
                }
                else {
                    String svg = AsciiDocCompiler.toSvg(model);
                    AsciiDocCompiler.saveSvgAsImage(svg, outputPath, dpi, Color.WHITE);
                }
            }

            Map<String, Object> imageAttributes = new HashMap<>();
            imageAttributes.put("target", fileName);
            imageAttributes.put("alt", "Preview of " + target);

            return createBlock(parent, "image", emptyList(), imageAttributes, new HashMap<>());
        } catch (IOException e) {
            return createBlock(parent, "paragraph", "Error generating preview: " + e.getMessage());
        }
    }
}
