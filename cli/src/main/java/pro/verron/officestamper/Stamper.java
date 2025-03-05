package pro.verron.officestamper;

import java.io.InputStream;
import java.io.OutputStream;

public interface Stamper {
    void stamp(Object context, InputStream templateStream, OutputStream outputStream);
}
