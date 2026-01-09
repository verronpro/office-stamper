package pro.verron.officestamper.test.utils;

import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.preset.Image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils.loadPowerPoint;
import static pro.verron.officestamper.utils.openpackaging.OpenpackagingUtils.loadWord;


/// A utility class for testing. Provides methods for retrieving InputStreams from specified resource paths. Typically
/// used for accessing test resources.
public class ResourceUtils {

    public static Image getImage(Path path) {
        return getImage(path, null);
    }

    public static Image getImage(Path path, @Nullable Integer size) {
        try {
            return new Image(getResource(path), size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// Retrieves an InputStream for the specified resource path.
    ///
    /// @param path the path of the resource
    ///
    /// @return an InputStream for the specified resource
    public static InputStream getResource(Path path) {
        try {
            var testRoot = Path.of("..", "test", "sources");
            var resolve = testRoot.resolve(path);
            return Files.newInputStream(resolve);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static WordprocessingMLPackage getWordResource(String path) {
        return getWordResource(Path.of(path));
    }

    public static WordprocessingMLPackage getWordResource(Path path) {
        var templateStream = getResource(path);
        return loadWord(templateStream);
    }

    public static PresentationMLPackage getPowerPointResource(Path path) {
        var templateStream = getResource(path);
        return loadPowerPoint(templateStream);
    }
}
