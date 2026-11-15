package pro.verron.officestamper.preset.resolvers.nulls;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.spi.Insert;
import pro.verron.officestamper.spi.OfficePart;

/// The Null2DefaultResolver class is an implementation of the
/// [ObjectResolver] interface
/// that resolves null objects by creating a run with a default text value.
///
/// @author Joseph Verron
/// @since 1.6.7
public record Null2DefaultResolver(String defaultValue)
        implements ObjectResolver {

    @Override
    public Insert resolve(OfficePart part, String expression, @Nullable Object object) {
        return new Insert(part.factory().run(defaultValue));
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object == null;
    }
}
