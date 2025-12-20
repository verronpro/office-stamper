package pro.verron.officestamper.preset.resolvers.nulls;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ObjectResolver;

import static pro.verron.officestamper.utils.wml.WmlFactory.newRun;

/// The [Null2PlaceholderResolver] class is an implementation of the ObjectResolver interface. It provides a way to
/// resolve null objects by not replacing their expression.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class Null2PlaceholderResolver
        implements ObjectResolver {

    private final String placeholderTemplate;

    /// Constructs a new [Null2PlaceholderResolver] with the specified placeholder template.
    ///
    /// @param template the template string to be used for formatting placeholders, where the expression will be
    ///         inserted using [String#format()]
    public Null2PlaceholderResolver(String template) {
        this.placeholderTemplate = template;
    }

    @Override
    public Insert resolve(DocxPart part, String expression, @Nullable Object object) {
        return new Insert(newRun(placeholderTemplate.formatted(expression)));
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object == null;
    }
}
