package pro.verron.officestamper.docx4j11;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import pro.verron.officestamper.api.OfficeStamper;
import pro.verron.officestamper.api.OfficeStamperConfiguration;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.preset.OfficeStamperConfigurations;

import java.io.IOException;

/// Deprecated backward-compatible bridge: an {@code OfficeStamper<WordprocessingMLPackage>}
/// that delegates to the docx4j-free contract {@link pro.verron.officestamper.spi.OfficeStamper}
/// (from officestamper-api) by wrapping the package in a {@link DocxDocument}.
///
/// <p>Kept for one release so existing callers keep compiling, then removed.
/// New code should depend only on the docx4j-free
/// {@link pro.verron.officestamper.spi.OfficeStampers#docxStamper()}.
///
/// @deprecated use {@link pro.verron.officestamper.spi.OfficeStampers#docxStamper()}
///             (the docx4j-free {@link pro.verron.officestamper.spi.OfficeStamper} from officestamper-api) instead.
@Deprecated(forRemoval = true)
public final class LegacyOfficeStamper implements OfficeStamper<WordprocessingMLPackage> {

    private final pro.verron.officestamper.spi.OfficeStamper<DocxDocument> delegate;

    /// Creates a shim with the {@link OfficeStamperConfigurations#full()} configuration.
    public LegacyOfficeStamper() {
        this(OfficeStamperConfigurations.full());
    }

    /// Creates a shim with the given configuration.
    ///
    /// @param configuration the stamper configuration
    public LegacyOfficeStamper(OfficeStamperConfiguration configuration) {
        this.delegate = new DocxStamper(configuration);
    }

    @Override
    public WordprocessingMLPackage stamp(WordprocessingMLPackage template, Object context) {
        var document = new DocxDocument(template);
        DocxDocument result;
        try {
            result = delegate.stamp(document, context);
        } catch (IOException e) {
            throw new OfficeStamperException(e);
        }
        return result.pkg();
    }
}
