package pro.verron.docxstamper.preset.resolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.docxstamper.api.ObjectResolver;

/**
 * The {@link Null2PlaceholderResolver} class is an implementation of the ObjectResolver interface.
 * It provides a way to resolve null objects by not replacing their placeholder string.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.7
 */
public class Null2PlaceholderResolver
        implements ObjectResolver {

    /* package */ Null2PlaceholderResolver() {
        //DO NOTHING
    }

    @Override
    public boolean canResolve(Object object) {
        return object == null;
    }

    @Override
    public R resolve(
            WordprocessingMLPackage document,
            String placeholder,
            Object object
    ) {
        return RunUtil.create(placeholder);
    }
}
