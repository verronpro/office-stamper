package pro.verron.docxstamper.preset.resolver;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;
import org.wickedsource.docxstamper.util.RunUtil;
import pro.verron.docxstamper.api.ObjectResolver;

/**
 * The Null2DefaultResolver class is an implementation of the
 * {@link ObjectResolver} interface
 * that resolves null objects by creating a run with a default text value.
 *
 * @author Joseph Verron
 * @version ${version}
 * @since 1.6.7
 */
public class Null2DefaultResolver
        implements ObjectResolver {

    private final String text;

    /* package */ Null2DefaultResolver(String text) {
        this.text = text;
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
        return RunUtil.create(text);
    }

    /**
     * Retrieves the default value of the {@link Null2DefaultResolver} object.
     *
     * @return the default value of the {@link Null2DefaultResolver} object as a String
     */
    public String defaultValue() {
        return text;
    }
}
