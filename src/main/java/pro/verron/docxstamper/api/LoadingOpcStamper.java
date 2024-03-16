package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.OpcPackage;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * OpcStamper is an interface that defines the contract for stamping
 * templates with context and writing the result to an {@link OutputStream}.
 *
 * @param <T> The type of the template that can be stamped.
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.4
 */
public class LoadingOpcStamper<T extends OpcPackage> {

    private final Function<InputStream, T> loader;
    private final OpcStamper<T> stamper;

    public LoadingOpcStamper(
            Function<InputStream, T> loader,
            OpcStamper<T> stamper
    ) {
        this.loader = loader;
        this.stamper = stamper;
    }

    /**
     * Stamps the template with the context and writes the result to the outputStream.
     *
     * @param inputStream  template to stamp
     * @param context      context to use for stamping
     * @param outputStream output stream to write the result to
     * @throws OpcStamperException if the stamping fails
     */
    public void stamp(
            InputStream inputStream,
            Object context,
            OutputStream outputStream
    ) throws OpcStamperException {
        T mlPackage = loader.apply(inputStream);
        stamper.stamp(mlPackage, context, outputStream);
    }
}
