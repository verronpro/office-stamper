package pro.verron.officestamper.preset.resolvers.nulls;

import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.utils.Inserts;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/// The [Null2PlaceholderResolver] class is an implementation of the ObjectResolver interface.
/// It provides a way to resolve null objects by not replacing their expression.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class Null2PlaceholderResolver
        implements ObjectResolver {

    private final String placeholderTemplate;

    public Null2PlaceholderResolver(String template) {
        this.placeholderTemplate = template;
    }

    @Override
    public Insert resolve(DocxPart document, String expression, @Nullable Object object) {
        return Inserts.of(newRun(placeholderTemplate.formatted(expression)));
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object == null;
    }
}
