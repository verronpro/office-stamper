package pro.verron.officestamper.api;

import org.docx4j.openpackaging.packages.OpcPackage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

/// This class implements the functionality of an [OfficeStamper] meant for dealing with streams of data. It delegates
/// the execution of the stamp operation to an [OfficeStamper] instance while providing the necessary mechanisms to work
/// with streams.
///
/// @param <T> The type of the template that can be stamped. This type must extend [OpcPackage].
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.4
public class StreamStamper<T extends OpcPackage> {

    /// Holds a reference to a function that takes in an [InputStream] and produces an instance of type [T].
    private final Function<InputStream, T> loader;

    /// Holds a reference to an [OfficeStamper] used to execute the stamp operation.
    private final OfficeStamper<T> stamper;

    /// Holds a reference to a consumer that exports the stamped document to an [OutputStream].
    private final BiConsumer<T, OutputStream> exporter;

    /// Constructs a new [StreamStamper] with the provided loader, stamper and exporter.
    ///
    /// @param loader A Function that takes in an [InputStream] and produces an instance of type [T].
    /// @param stamper An [OfficeStamper] used to execute the stamp operation.
    /// @param exporter A [BiConsumer] that exports the stamped document to an [OutputStream].
    public StreamStamper(
            Function<InputStream, T> loader,
            OfficeStamper<T> stamper,
            BiConsumer<T, OutputStream> exporter
    ) {
        this.loader = loader;
        this.stamper = stamper;
        this.exporter = exporter;
    }

    /// Stamps the template present in the given InputStream with the context given and writes the result to the
    /// provided [OutputStream]. This method first uses the loader to load the template from the [InputStream] into a
    /// type [T] instance, then uses the stamper to perform the stamp operation using the template and context, writing
    /// the result out to the OutputStream.
    ///
    /// @param inputStream template to stamp
    /// @param context context to use for stamping
    /// @param outputStream output stream to write the result to
    ///
    /// @throws OfficeStamperException if the stamping fails for any reason
    public void stamp(InputStream inputStream, Object context, OutputStream outputStream)
            throws OfficeStamperException {
        var template = loader.apply(inputStream);
        var stamped = stamper.stamp(template, context);
        exporter.accept(stamped, outputStream);
    }
}
