package pro.verron.officestamper.asciidoc;

import javafx.scene.Scene;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/// Facade utilities to parse AsciiDoc and compile it to different targets.
public final class AsciiDocCompiler {

    public static final AsciiDocToHtml MODEL_TO_HTML = new AsciiDocToHtml();
    public static final AsciiDocToFx MODEL_TO_SCENE = new AsciiDocToFx();
    public static final AsciiDocParser ASCIIDOC_TO_MODEL = new AsciiDocParser();
    public static final AsciiDocToDocx MODEL_TO_DOCX = new AsciiDocToDocx();
    private static final AsciiDocToText MODEL_TO_ASCIIDOC = new AsciiDocToText();

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

    /// Compiles the AsciiDoc source text directly to a JavaFX Scene.
    ///
    /// @param asciidoc source text
    ///
    /// @return scene with rendered content
    public static Scene toScene(String asciidoc) {
        var model = ASCIIDOC_TO_MODEL.apply(asciidoc);
        return MODEL_TO_SCENE.apply(model);
    }

    /// Compiles the parsed model to a JavaFX Scene.
    ///
    /// @param model parsed model
    ///
    /// @return scene with rendered content
    public static Scene toScene(AsciiDocModel model) {
        return MODEL_TO_SCENE.apply(model);
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
        var model = toModel(pkg);
        return MODEL_TO_ASCIIDOC.apply(model);
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

    /// Compiles the parsed model to its textual AsciiDoc representation.
    ///
    /// @param model parsed model
    ///
    /// @return textual representation
    public static String toAsciidoc(AsciiDocModel model) {
        return MODEL_TO_ASCIIDOC.apply(model);
    }
}
