package pro.verron.officestamper.asciidoc;

import javafx.scene.Scene;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/// Facade utilities to parse AsciiDoc and compile it to different targets.
public final class AsciiDocCompiler {

    static {
        System.setProperty("jruby.compat.version", "RUBY1_9");
        System.setProperty("jruby.compile.mode", "OFF");
    }
    private AsciiDocCompiler() {

    }

    /// Compiles the AsciiDoc source text directly to a WordprocessingMLPackage.
    ///
    /// @param asciidoc source text
    ///
    /// @return package with rendered content
    public static WordprocessingMLPackage toDocx(String asciidoc) {
        return toDocx(parse(asciidoc));
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
    public static AsciiDocModel parse(String asciidoc) {
        return AsciiDocParser.parse(asciidoc);
    }

    /// Compiles the AsciiDoc source text directly to a JavaFX Scene.
    ///
    /// @param asciidoc source text
    ///
    /// @return scene with rendered content
    public static Scene toScene(String asciidoc) {
        return toScene(parse(asciidoc));
    }

    /// Compiles the parsed model to a JavaFX Scene.
    ///
    /// @param model parsed model
    ///
    /// @return scene with rendered content
    public static Scene toScene(AsciiDocModel model) {
        return AsciiDocToFx.compileToScene(model);
    }

    /// Compiles a WordprocessingMLPackage into the textual AsciiDoc representation used by tests. This mirrors the
    /// legacy Stringifier output to preserve expectations.
    ///
    /// @param pkg a Word document package
    ///
    /// @return textual representation
    public static String toAsciiDoc(WordprocessingMLPackage pkg) {
        return DocxToAsciiDoc.compile(pkg, AsciiDocDialect.COMPAT);
    }

    /// Compiles a WordprocessingMLPackage into AsciiDoc using the specified dialect.
    ///
    /// @param pkg a Word document package
    /// @param dialect output dialect (compat or adoc)
    ///
    /// @return textual representation
    public static String toAsciiDoc(WordprocessingMLPackage pkg, AsciiDocDialect dialect) {
        return DocxToAsciiDoc.compile(pkg, dialect);
    }
}
