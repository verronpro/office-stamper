package pro.verron.officestamper.asciidoc;

import javafx.scene.Scene;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/// Facade utilities to parse AsciiDoc and compile it to different targets.
public final class AsciiDocCompiler {

    public static final DocxToAsciiDoc DOCX_TO_ASCII_DOC_MODEL = new DocxToAsciiDoc();

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
        return AsciiDocToDocx.compileToPackage(model);
    }

    /// Parses AsciiDoc source text into an [AsciiDocModel].
    ///
    /// @param asciidoc source text
    ///
    /// @return parsed model
    public static AsciiDocModel toModel(String asciidoc) {
        return AsciiDocParser.parse(asciidoc);
    }

    /// Compiles the AsciiDoc source text directly to a JavaFX Scene.
    ///
    /// @param asciidoc source text
    ///
    /// @return scene with rendered content
    public static Scene toScene(String asciidoc) {
        return toScene(toModel(asciidoc));
    }

    /// Compiles the parsed model to a JavaFX Scene.
    ///
    /// @param model parsed model
    ///
    /// @return scene with rendered content
    public static Scene toScene(AsciiDocModel model) {
        return AsciiDocToFx.compileToScene(model);
    }

    /// Compiles the AsciiDoc source text directly to HTML.
    ///
    /// @param asciidoc source text
    ///
    /// @return HTML representation
    public static String toHtml(String asciidoc) {
        return toHtml(toModel(asciidoc));
    }

    /// Compiles the parsed model to HTML.
    ///
    /// @param model parsed model
    ///
    /// @return HTML representation
    public static String toHtml(AsciiDocModel model) {
        return AsciiDocToHtml.compileToHtml(model);
    }

    /// Compiles a WordprocessingMLPackage into the textual AsciiDoc representation used by tests. This mirrors the
    /// legacy Stringifier output to preserve expectations.
    ///
    /// @param pkg a Word document package
    ///
    /// @return textual representation
    public static String toText(WordprocessingMLPackage pkg) {
        return toText(toModel(pkg));
    }

    /// Compiles the parsed model to its textual AsciiDoc representation.
    ///
    /// @param model parsed model
    ///
    /// @return textual representation
    public static String toText(AsciiDocModel model) {
        return AsciiDocToText.compileToText(model);
    }

    /// Parses a Word document into an [AsciiDocModel].
    ///
    /// @param pkg a Word document package
    ///
    /// @return parsed model
    public static AsciiDocModel toModel(WordprocessingMLPackage pkg) {
        return DOCX_TO_ASCII_DOC_MODEL.apply(pkg);
    }
}
