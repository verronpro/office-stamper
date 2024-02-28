package org.wickedsource.docxstamper.replace.typeresolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.docxstamper.api.ObjectResolver;
import pro.verron.docxstamper.preset.resolver.Resolvers;

/**
 * The LegacyFallbackResolver served as a fallback when there was no ITypeResolver available for a certain type.
 * It was capable of mapping all objects to their String value.
 *
 * @author Joseph Verron
 *
 * @version ${version}
 *
 * @deprecated as of version 1.6.7, use
 * {@link Resolvers#fallback()} instead.
 * LegacyFallbackResolver
 * was capable of mapping any object to their String representation.
 * Now, this is more streamlined and manageable using {@link Resolvers#fallback()}.
 */
@Deprecated(since = "1.6.7", forRemoval = true)
public class LegacyFallbackResolver
        implements ObjectResolver {

    private static String format(Object object) {
        if (object == null) return "";
        return String.valueOf(object);
    }

	@Override
    public boolean canResolve(Object object) {
        return true;
    }

    @Override
    public R resolve(
            WordprocessingMLPackage document,
            String placeholder,
            Object object
    ) {
        return RunUtil.create(format(object));
    }
}
