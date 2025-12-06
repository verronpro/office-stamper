package pro.verron.officestamper.asciidoc;

import javafx.scene.Scene;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

/**
 * Facade utilities to parse AsciiDoc and compile it to different targets.
 */
public final class AsciiDocCompiler {
    private AsciiDocCompiler() {
    }

    /**
     * Compiles the AsciiDoc source text directly to a WordprocessingMLPackage.
     *
     * @param asciidoc source text
     *
     * @return package with rendered content
     */
    public static WordprocessingMLPackage toDocx(String asciidoc) {
        return toDocx(parse(asciidoc));
    }

    /**
     * Compiles the parsed model to a WordprocessingMLPackage.
     *
     * @param model parsed model
     *
     * @return package with rendered content
     */
    public static WordprocessingMLPackage toDocx(AsciiDocModel model) {
        return AsciiDocToDocx.compileToPackage(model);
    }

    /**
     * Parses AsciiDoc source text into an {@link AsciiDocModel}.
     *
     * @param asciidoc source text
     *
     * @return parsed model
     */
    public static AsciiDocModel parse(String asciidoc) {
        return AsciiDocParser.parse(asciidoc);
    }

    /**
     * Compiles the AsciiDoc source text directly to a JavaFX Scene.
     *
     * @param asciidoc source text
     *
     * @return scene with rendered content
     */
    public static Scene toScene(String asciidoc) {
        return toScene(parse(asciidoc));
    }

    /**
     * Compiles the parsed model to a JavaFX Scene.
     *
     * @param model parsed model
     *
     * @return scene with rendered content
     */
    public static Scene toScene(AsciiDocModel model) {
        return AsciiDocToFx.compileToScene(model);
    }
}
