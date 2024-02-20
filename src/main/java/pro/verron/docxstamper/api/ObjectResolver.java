package pro.verron.docxstamper.api;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.R;

/**
 * The ObjectResolver interface provides a contract for resolving objects to create a run
 * with the resolved content. It includes methods to check if an object can be resolved
 * and to actually resolve an object to a run.
 *
 * @author Joseph Verron
 * @version 1.6.7
 * @since 1.6.7
 */
public interface ObjectResolver {
    boolean canResolve(Object object);

    R resolve(
            WordprocessingMLPackage document,
            String placeholder,
            Object object
    );
}
