package pro.verron.officestamper.preset.resolvers.nulls;

import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ObjectResolver;

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

    /// The Null2DefaultResolver class is an implementation of the ObjectResolver interface
    /// that resolves null objects by creating a run with a default text value.
    ///
    /// @param text The default text value to be used when the resolved object is null
    public Null2DefaultResolver {
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object == null;
    }

    @Override
    public R resolve(
            DocxPart document,
            String expression,
            Object object
    ) {
        return newRun(text);
    }

    /// Retrieves the default value of the [Null2DefaultResolver] object.
    ///
    /// @return the default value of the [Null2DefaultResolver] object as a String
    public String defaultValue() {
        return text;
    }
}
