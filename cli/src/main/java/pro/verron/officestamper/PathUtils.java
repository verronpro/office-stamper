package pro.verron.officestamper;

import java.nio.file.Path;

public class PathUtils {
    static String baseName(Path f) {
        var fileName = f.getFileName();
        var base = fileName.toString();
        var idx = base.lastIndexOf('.');
        if (idx > 0) base = base.substring(0, idx);
        return base;
    }
}
