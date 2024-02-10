package pro.verron.docxstamper.accessors;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * <p>SimpleGetter class.</p>
 *
 * @author Joseph Verron
 * @version 1.6.6
 * @since 1.6.6
 */
public class SimpleGetter implements PropertyAccessor {

    private final String fieldName;

    private final Object value;

    /**
     * <p>Constructor for SimpleGetter.</p>
     *
     * @param fieldName a {@link java.lang.String} object
     * @param value     a {@link java.lang.Object} object
     */
    public SimpleGetter(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) {
        if (name.equals(this.fieldName)) {
            return new TypedValue(value);
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) {
    }
}
