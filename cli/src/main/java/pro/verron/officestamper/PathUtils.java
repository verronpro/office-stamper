package pro.verron.officestamper;

import pro.verron.officestamper.api.OfficeStamperException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.newOutputStream;

public class PathUtils {
    static String baseName(Path f) {
        var fileName = f.getFileName();
        var base = fileName.toString();
        var idx = base.lastIndexOf('.');
        if (idx > 0) base = base.substring(0, idx);
        return base;
    }

    static int depthCompare(Path p1, Path p2) {
        return p2.getNameCount() - p1.getNameCount();
    }

    static InputStream streamFile(Path path) {
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    static OutputStream createOutputStream(Path path) {
        try {
            var parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            return newOutputStream(path);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
    }

    static String extension(Path f) {
        var fileName = f.getFileName();
        var base = fileName.toString();
        var idx = base.lastIndexOf('.');
        if (idx > 0) base = base.substring(idx + 1);
        return base;
    }

    static Path computeOutputPath(String output, String itemName, String desiredExt) {
        var out = Path.of(output);
        // If output is an existing directory, place <itemName><ext> inside it
        if (Files.exists(out) && Files.isDirectory(out)) return out.resolve(itemName + desiredExt);
        var fn = out.getFileName() == null ? output : out.getFileName().toString();
        var dot = fn.lastIndexOf('.');
        if (dot > 0) {
            var base = fn.substring(0, dot);
            // Normalize to template extension
            var newName = base + "-" + itemName + desiredExt;
            var parent = out.getParent();
            return parent == null ? Path.of(newName) : parent.resolve(newName);
        } else return out.resolve(itemName + desiredExt); // Treat as directory path (may or may not exist)
    }
}
