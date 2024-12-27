package pro.verron.officestamper.preset;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Map;

public class MapAccessor
        implements PropertyAccessor {

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class<?>[]{Map.class};
    }

    @Override
    public boolean canRead(EvaluationContext context, @Nullable Object target, String name) {
        return (target instanceof Map<?, ?> map && map.containsKey(name));
    }

    @Override
    public TypedValue read(EvaluationContext context, @Nullable Object target, String name)
            throws AccessException {
        Assert.state(target instanceof Map, "Target must be of type Map");
        Map<?, ?> map = (Map<?, ?>) target;
        Object value = map.get(name);
        if (value == null && !map.containsKey(name)) {
            throw new MapAccessException(name);
        }
        return new TypedValue(value);
    }

    @Override
    public boolean canWrite(EvaluationContext context, @Nullable Object target, String name) {
        return target instanceof Map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(EvaluationContext context, @Nullable Object target, String name, @Nullable Object newValue) {
        Assert.state(target instanceof Map, "Target must be of type Map");
        Map<Object, Object> map = (Map<Object, Object>) target;
        map.put(name, newValue);
    }

    /// Exception thrown from `read` in order to reset a cached
    /// PropertyAccessor, allowing other accessors to have a try.
    private static class MapAccessException
            extends AccessException {

        private final String key;

        public MapAccessException(String key) {
            super("");
            this.key = key;
        }

        @Override
        public String getMessage() {
            return "Map does not contain a value for key '" + this.key + "'";
        }
    }
}
