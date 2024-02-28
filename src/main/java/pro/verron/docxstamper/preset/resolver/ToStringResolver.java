package pro.verron.docxstamper.preset.resolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.docxstamper.api.ObjectResolver;

/**
 * This class is an implementation of the {@link ObjectResolver} interface
 * that resolves objects by converting them to a string representation using the
 * {@link Object#toString()} method and creating a new run with the resolved content.
 */
public class ToStringResolver
        implements ObjectResolver {
    @Override
    public boolean canResolve(Object object) {
        return object != null;
    }

    @Override
    public R resolve(
            WordprocessingMLPackage document,
            String placeholder,
            Object object
    ) {
        return RunUtil.create(String.valueOf(object));
    }
}
