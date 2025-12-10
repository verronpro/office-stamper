package pro.verron.officestamper.preset.resolvers.nulls;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ObjectResolver;

import static pro.verron.officestamper.utils.wml.WmlFactory.newRun;

/// The Null2DefaultResolver class is an implementation of the
/// [ObjectResolver] interface
/// that resolves null objects by creating a run with a default text value.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public record Null2DefaultResolver(String defaultValue)
        implements ObjectResolver {

    @Override
    public Insert resolve(DocxPart part, String expression, @Nullable Object object) {
        return new Insert(newRun(defaultValue));
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object == null;
    }
}
