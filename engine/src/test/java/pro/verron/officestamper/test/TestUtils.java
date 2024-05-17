package pro.verron.officestamper.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * A utility class for testing.
 * Provides methods for retrieving InputStreams from specified resource paths.
 * Typically used for accessing test resources.
 */
public class TestUtils {

    /**
     * Retrieves an InputStream for the specified resource path.
     *
     * @param path the path of the resource
     *
     * @return an InputStream for the specified resource
     */
    public static InputStream getResource(String path) {
        return getResource(Path.of(path));
    }


    /**
     * Retrieves an InputStream for the specified resource path.
     *
     * @param path the path of the resource
     *
     * @return an InputStream for the specified resource
     */
    public static InputStream getResource(Path path) {
        try {
            var testRoot = Path.of("..", "test", "sources");
            var resolve = testRoot.resolve(path);
            return Files.newInputStream(resolve);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
