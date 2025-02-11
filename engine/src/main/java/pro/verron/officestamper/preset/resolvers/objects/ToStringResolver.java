package pro.verron.officestamper.preset.resolvers.objects;

import org.docx4j.wml.R;
import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.ObjectResolver;

import static pro.verron.officestamper.utils.WmlFactory.newRun;

/// This class is an implementation of the [ObjectResolver] interface
/// that resolves objects by converting them to a string representation using the
/// [#toString()] method and creating a new run with the resolved content.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class ToStringResolver
        implements ObjectResolver {
    @Override
    public boolean canResolve(@Nullable Object object) {
        return object != null;
    }

    @Override
    public R resolve(
            DocxPart document,
            String expression,
            Object object
    ) {
        return newRun(String.valueOf(object));
    }
}
