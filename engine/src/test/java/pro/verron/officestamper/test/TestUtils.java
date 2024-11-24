package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import pro.verron.officestamper.preset.Image;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static pro.verron.officestamper.utils.WmlFactory.*;


/// A utility class for testing.
/// Provides methods for retrieving InputStreams from specified resource paths.
/// Typically used for accessing test resources.
public class TestUtils {

    /// Retrieves an InputStream for the specified resource path.
    ///
    /// @param path the path of the resource
    ///
    /// @return an InputStream for the specified resource
    public static InputStream getResource(String path) {
        return getResource(Path.of(path));
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

    public static InputStream makeResource(String content)
            throws Docx4JException, IOException {
        var aPackage = WordprocessingMLPackage.createPackage();
        var mainDocumentPart = aPackage.getMainDocumentPart();
        content.lines()
               .forEach(line -> {
                   var split = line.split("\\|TAB\\|");
                   var value = split[0];
                   var run = newRun(value);
                   int i = 1;
                   while (i < split.length) {
                       var s = split[i];
                       var text = new Text();
                       text.setValue(s);
                       run.getContent()
                          .add(new R.Tab());
                       run.getContent()
                          .add(text);
                       i++;
                   }
                       var p1 = new P();
        p1.getContent()
          .add(run);
        mainDocumentPart.addObject(p1);
               });
        var outputStream = IOStreams.getOutputStream();
        aPackage.save(outputStream);
        return IOStreams.getInputStream(outputStream);
    }

    static Image getImage(Path path) {
        try {
            return new Image(getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Image getImage(Path path, int size) {
        try {
            return new Image(getResource(path), size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
