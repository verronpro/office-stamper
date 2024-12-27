package pro.verron.officestamper.preset.resolvers.nulls;

import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.api.OfficeStamperException;
import pro.verron.officestamper.api.Placeholder;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/// The [Null2PlaceholderResolver] class is an implementation of the ObjectResolver interface.
/// It provides a way to resolve null objects by not replacing their expression.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class Null2PlaceholderResolver
        implements ObjectResolver {

    public Null2PlaceholderResolver() {
        //DO NOTHING
    }

    @Override
    public R resolve(
            DocxPart document,
            Placeholder placeholder,
            Object object
    ) {
        return newRun(placeholder.expression());
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
        throw new OfficeStamperException("Should not be called");
    }
}
