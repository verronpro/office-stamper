package pro.verron.officestamper.asciidoc;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/// Facade utilities to parse AsciiDoc and compile it to different targets.
public final class AsciiDocCompiler {

    private static final AsciiDocToHtml MODEL_TO_HTML = new AsciiDocToHtml();
    private static final AsciiDocToSvg MODEL_TO_SVG = new AsciiDocToSvg();
    private static final AsciiDocParser ASCIIDOC_TO_MODEL = new AsciiDocParser();
    private static final AsciiDocToDocx MODEL_TO_DOCX = new AsciiDocToDocx();

    private AsciiDocCompiler() {
        throw new IllegalStateException("Utility class");
    }

    /// Compiles the AsciiDoc source text directly to a WordprocessingMLPackage.
    ///
    /// @param asciidoc source text
    ///
    /// @return package with rendered content
    public static WordprocessingMLPackage toDocx(String asciidoc) {
        return toDocx(toModel(asciidoc));
    }

    /// Compiles the parsed model to a WordprocessingMLPackage.
    ///
    /// @param model parsed model
    ///
    /// @return package with rendered content
    public static WordprocessingMLPackage toDocx(AsciiDocModel model) {
        return MODEL_TO_DOCX.apply(model);
    }

    /// Parses AsciiDoc source text into an [AsciiDocModel].
    ///
    /// @param asciidoc source text
    ///
    /// @return parsed model
    public static AsciiDocModel toModel(String asciidoc) {
        return ASCIIDOC_TO_MODEL.apply(asciidoc);
    }

    /// Compiles the parsed model to an SVG document.
    ///
    /// @param model parsed model
    ///
    /// @return SVG representation
    public static String toSvg(AsciiDocModel model) {
        return MODEL_TO_SVG.apply(model);
    }

    /// Compiles the AsciiDoc source text directly to HTML.
    ///
    /// @param asciidoc source text
    ///
    /// @return HTML representation
    public static String toHtml(String asciidoc) {
        var model = ASCIIDOC_TO_MODEL.apply(asciidoc);
        return MODEL_TO_HTML.apply(model);
    }

    /// Compiles the parsed model to HTML.
    ///
    /// @param model parsed model
    ///
    /// @return HTML representation
    public static String toHtml(AsciiDocModel model) {
        return MODEL_TO_HTML.apply(model);
    }

    /// Compiles a WordprocessingMLPackage into the textual AsciiDoc representation used by tests. This mirrors the
    /// legacy Stringifier output to preserve expectations.
    ///
    /// @param pkg a Word document package
    ///
    /// @return textual representation
    public static String toAsciidoc(WordprocessingMLPackage pkg) {
        return toAsciidoc(pkg, false);
    }

    /// Converts the given WordprocessingMLPackage into its textual AsciiDoc representation.
    ///
    /// @param pkg a Word document package
    /// @param skipComments whether to omit comments from the output
    /// @return the textual AsciiDoc representation of the Word document package
    public static String toAsciidoc(WordprocessingMLPackage pkg, boolean skipComments) {
        var model = toModel(pkg);
        return toAsciidoc(model, skipComments);
    }

    /// Parses a Word document into an [AsciiDocModel].
    ///
    /// @param pkg a Word document package
    ///
    /// @return parsed model
    public static AsciiDocModel toModel(WordprocessingMLPackage pkg) {
        var compiler = new DocxToAsciiDoc(pkg);
        return compiler.apply(pkg);
    }

    /// Converts the given AsciiDoc model into its textual AsciiDoc representation.
    ///
    /// @param model the parsed AsciiDoc model to be converted
    /// @param skipComments whether to omit comments from the output
    /// @return the textual AsciiDoc representation of the model
    public static String toAsciidoc(AsciiDocModel model, boolean skipComments) {
        return new AsciiDocToText(skipComments).apply(model);
    }

    /// Converts the given AsciiDoc model into its textual AsciiDoc representation.
    ///
    /// @param model the parsed AsciiDoc model to be converted
    /// @return the textual AsciiDoc representation of the model
    public static String toAsciidoc(AsciiDocModel model) {
        return new AsciiDocToText(false).apply(model);
    }

    /// Reads AsciiDoc source from an input stream and compiles it to SVG.
    ///
    /// @param input source stream
    ///
    /// @return SVG representation
    public static String toSvg(InputStream input) {
        try {
            return toSvg(new String(input.readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read AsciiDoc input stream", e);
        }
    }

    /// Compiles the AsciiDoc source text directly to an SVG document.
    ///
    /// @param asciidoc source text
    ///
    /// @return SVG representation
    public static String toSvg(String asciidoc) {
        var model = ASCIIDOC_TO_MODEL.apply(asciidoc);
        return MODEL_TO_SVG.apply(model);
    }

    /// Saves the AsciiDoc source text directly to a PNG image file.
    ///
    /// @param asciidoc source text
    /// @param path path to save the image
    public static void toImage(String asciidoc, Path path) {
        saveSvgAsImage(toSvg(asciidoc), path);
    }

    /// Saves the given SVG document as a PNG image file.
    ///
    /// @param svg SVG source
    /// @param path path to save the image
    public static void saveSvgAsImage(String svg, Path path) {
        var transcoder = new PNGTranscoder();
        var input = new TranscoderInput(new StringReader(svg));
        try (OutputStream output = Files.newOutputStream(path)) {
            transcoder.transcode(input, new TranscoderOutput(output));
        } catch (IOException e) {
            throw new RuntimeException("IO error while writing PNG image", e);
        } catch (TranscoderException e) {
            throw new RuntimeException("Failed to transcode SVG image", e);
        }
    }
}
