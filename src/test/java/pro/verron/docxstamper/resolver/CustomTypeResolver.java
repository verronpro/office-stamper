package pro.verron.docxstamper.resolver;

import org.wickedsource.docxstamper.replace.typeresolver.AbstractToTextResolver;
import pro.verron.docxstamper.utils.context.Contexts;

/**
 * <p>CustomTypeResolver class.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
public class CustomTypeResolver extends AbstractToTextResolver<Contexts.CustomType> {
    /**
     * {@inheritDoc}
     */
    @Override
    protected String resolveStringForObject(Contexts.CustomType object) {
        return "foo";
    }
}
