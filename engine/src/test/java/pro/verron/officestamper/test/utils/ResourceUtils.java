package pro.verron.officestamper.test.utils;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.preset.Image;
import pro.verron.officestamper.utils.pml.PptxDocument;
import pro.verron.officestamper.utils.wml.DocxDocument;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


/// A utility class for testing. Provides methods for retrieving InputStreams from specified resource paths. Typically
/// used for accessing test resources.
public class ResourceUtils {

    /// Default constructor.
    public ResourceUtils() {
    }

    /// Retrieves an image from the specified resource path.
    ///
    /// @param path the path of the resource
    /// @return an image for the specified resource
    public static Image getImage(Path path) {
        return getImage(path, null);
    }

    /// Retrieves an image from the specified resource path.
    ///
    /// @param path the path of the resource
    /// @param size the size of the image
    /// @return an image for the specified resource
    public static Image getImage(Path path, @Nullable Integer size) {
        return new Image(getResource(path), size);
    }

    /// Retrieves an InputStream for the specified resource path.
    ///
    /// @param path the path of the resource
    /// @return an InputStream for the specified resource
    public static InputStream getResource(Path path) {
        try {
            var testRoot = Path.of("..", "src", "test", "resources");
            var resolve = testRoot.resolve(path);
            return Files.newInputStream(resolve);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// Retrieves a WordprocessingMLPackage from the specified resource path.
    ///
    /// @param path the path of the resource
    /// @return a WordprocessingMLPackage for the specified resource
    public static DocxDocument getWordResource(String path) {
        return getWordResource(Path.of(path));
    }

    /// Retrieves a WordprocessingMLPackage from the specified resource path.
    ///
    /// @param path the path of the resource
    /// @return a WordprocessingMLPackage for the specified resource
    public static DocxDocument getWordResource(Path path) {
        var templateStream = getResource(path);
        return DocxDocument.load(templateStream);
    }

    /// Retrieves a PresentationMLPackage from the specified resource path.
    ///
    /// @param path the path of the resource
    /// @return a PresentationMLPackage for the specified resource
    public static PptxDocument getPowerPointResource(Path path) {
        var templateStream = getResource(path);
        return PptxDocument.load(templateStream);
    }
}
