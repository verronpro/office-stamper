package pro.verron.officestamper.preset.resolvers.nulls;

import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.utils.Inserts;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/// The Null2DefaultResolver class is an implementation of the
/// [ObjectResolver] interface
/// that resolves null objects by creating a run with a default text value.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public record Null2DefaultResolver(String text)
        implements ObjectResolver {

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object == null;
    }

    @Override
    public Insert resolve(
            DocxPart document,
            String expression,
            Object object
    ) {
        return Inserts.of(newRun(text));
    }

    /// Retrieves the default value of the [Null2DefaultResolver] object.
    ///
    /// @return the default value of the [Null2DefaultResolver] object as a String
    public String defaultValue() {
        return text;
    }
}
