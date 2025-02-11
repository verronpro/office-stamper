package pro.verron.officestamper.test;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.StreamStamper;
import pro.verron.officestamper.preset.OfficeStampers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/// Common methods to interact with docx documents.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.5
public final class TestDocxStamper<T> {

    private final StreamStamper<WordprocessingMLPackage> stamper;

    /// Constructor for TestDocxStamper.
    ///
    /// @param config a [OfficeStamperConfiguration] object
    ///
    /// @since 1.6.6
    public TestDocxStamper(OfficeStamperConfiguration config) {
        stamper = OfficeStampers.docxStamper(config);
    }

    /// Stamps the given template resolving the expressions within the template against the specified context.
    /// Returns the resulting document after it has been saved and loaded
    /// again to ensure that changes in the DOCX4J
    /// object structure were really transported into the XML of the .docx file.
    ///
    /// @param template a [InputStream] object
    /// @param context  a T object
    ///
    /// @return a [WordprocessingMLPackage] object
    ///
    /// @throws IOException     if any.
    /// @throws Docx4JException if any.
    /// @since 1.6.6
    public WordprocessingMLPackage stampAndLoad(
            InputStream template,
            T context
    ) throws IOException, Docx4JException {
        OutputStream out = IOStreams.getOutputStream();
        stamper.stamp(template, context, out);
        InputStream in = IOStreams.getInputStream(out);
        return WordprocessingMLPackage.load(in);
    }

    /// @param template a [InputStream] object
    /// @param context  a T object
    /// @return a [List] object
    /// @since 1.6.6
    public String stampAndLoadAndExtract(InputStream template, T context) {
        var wordprocessingMLPackage = streamElements(template, context);
        return new Stringifier(() -> wordprocessingMLPackage).stringify(wordprocessingMLPackage);
    }

    private WordprocessingMLPackage streamElements(
            InputStream template,
            T context
    ) {
        try {
            var out = IOStreams.getOutputStream();
            stamper.stamp(template, context, out);
            var in = IOStreams.getInputStream(out);
            return WordprocessingMLPackage.load(in);
        } catch (Docx4JException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
