package pro.verron.officestamper.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

/// Stamps documents from streams, delegating the stamp operation to an [OfficeStamper] while
/// providing the mechanisms to work with streams. The loader is format-specific and supplied by
/// the provider; the exporter can simply be `(doc, out) -> doc.save(out)`.
public class StreamStamper {

    private final Function<InputStream, OfficeDocument> loader;
    private final OfficeStamper stamper;
    private final BiConsumer<OfficeDocument, OutputStream> exporter;

    /// Constructs a new stream stamper.
    ///
    /// @param loader   loads a template from an input stream into a document
    /// @param stamper  the stamper performing the stamp operation
    /// @param exporter exports the stamped document to an output stream
    public StreamStamper(
            Function<InputStream, OfficeDocument> loader,
            OfficeStamper stamper,
            BiConsumer<OfficeDocument, OutputStream> exporter
    ) {
        this.loader = loader;
        this.stamper = stamper;
        this.exporter = exporter;
    }

    /// Stamps the template present in the input stream with the given context and writes the result to the output stream.
    ///
    /// @param inputStream  the template to stamp
    /// @param context      the context used for stamping
    /// @param outputStream the stream to write the result to
    public void stamp(InputStream inputStream, Object context, OutputStream outputStream) {
        var template = loader.apply(inputStream);
        var stamped = stamper.stamp(template, context);
        exporter.accept(stamped, outputStream);
    }
}
