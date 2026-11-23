package pro.verron.officestamper.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

/// Stamps documents from streams, delegating the stamp operation to an {@link OfficeStamper} while
/// providing the mechanisms to work with streams.
///
/// @param <T> the document type, which must extend {@link OfficeDocument}
/// @param <T> the native document type this stamper operates on
public class StreamStamper<T> {

    private final Function<InputStream, T> loader;
    private final OfficeStamper<T> stamper;
    private final BiConsumer<T, OutputStream> exporter;

    /// Constructs a new stream stamper.
    ///
    /// @param loader   loads a template from an input stream into a document
    /// @param stamper  the stamper performing the stamp operation
    /// @param exporter exports the stamped document to an output stream
    public StreamStamper(Function<InputStream, T> loader, OfficeStamper<T> stamper, BiConsumer<T, OutputStream> exporter) {
        this.loader = loader;
        this.stamper = stamper;
        this.exporter = exporter;
    }

    /// Stamps the template present in the input stream with the given context and writes the result to the output stream.
    ///
    /// @param inputStream  the template to stamp
    /// @param context      the context used for stamping
    /// @param outputStream the stream to write the result to
    public void stamp(InputStream inputStream, Object context, OutputStream outputStream) throws IOException {
        var template = loader.apply(inputStream);
        var stamped = stamper.stamp(template, context);
        exporter.accept(stamped, outputStream);
    }
}
