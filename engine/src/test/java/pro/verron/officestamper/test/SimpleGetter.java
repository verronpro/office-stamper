package pro.verron.officestamper.test;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;

/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.6
public class SimpleGetter
        implements PropertyAccessor {

    private final String fieldName;

    private final Object value;

    /// Constructor for SimpleGetter.
    ///
    /// @param fieldName a [java.lang.String] object
    /// @param value     a [java.lang.Object] object
    public SimpleGetter(String fieldName, Object value) {
        this.fieldName = fieldName;
        this.value = value;
    }

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return null;
    }

    @Override
    public boolean canRead(@NonNull EvaluationContext context, Object target, @NonNull String name) {
        return true;
    }

    @Override
    @NonNull
    public TypedValue read(@NonNull EvaluationContext context, Object target, String name) {
        if (name.equals(this.fieldName)) {
            return new TypedValue(value);
        }
        else {
            return TypedValue.NULL;
        }
    }

    @Override
    public boolean canWrite(@NonNull EvaluationContext context, Object target, @NonNull String name) {
        return false;
    }

    @Override
    public void write(@NonNull EvaluationContext context, Object target, @NonNull String name, Object newValue) {
    }
}
