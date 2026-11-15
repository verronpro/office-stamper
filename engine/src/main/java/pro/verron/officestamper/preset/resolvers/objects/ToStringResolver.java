package pro.verron.officestamper.preset.resolvers.objects;

import org.jspecify.annotations.Nullable;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.spi.Element;
import pro.verron.officestamper.spi.Insert;
import pro.verron.officestamper.spi.OfficePart;

import java.util.ArrayList;
import java.util.List;

/// This class is an implementation of the [ObjectResolver] interface that resolves objects by converting them to a
/// string representation using the [Object#toString()] method and creating a new run with the resolved content.
///
/// @author Joseph Verron
/// @since 1.6.7
public class ToStringResolver
        implements ObjectResolver {

    private final String linebreakPlaceholder;


    /// Creates a new instance of the [ToStringResolver] class with the specified line break placeholder.
    ///
    /// @param linebreakPlaceholder the placeholder string used to identify line breaks within the string
    ///         representation of an object. This placeholder will be replaced with actual line break elements in the
    ///         resulting document.
    public ToStringResolver(String linebreakPlaceholder) {
        this.linebreakPlaceholder = linebreakPlaceholder;
    }

    @Override
    public Insert resolve(OfficePart part, String expression, @Nullable Object object) {
        var string = String.valueOf(object);
        var factory = part.factory();

        var split = string.split(linebreakPlaceholder);
        if (split.length == 1) return new Insert(factory.run(string));
        var elements = new ArrayList<Element>();
        for (int i = 0; i < split.length - 1; i++) {
            elements.add(factory.lineBreak(split[i]));
        }
        elements.add(factory.run(split[split.length - 1]));
        return new Insert(elements);
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object != null;
    }
}
