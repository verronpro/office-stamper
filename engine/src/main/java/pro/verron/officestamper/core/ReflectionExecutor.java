package pro.verron.officestamper.core;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * A record encapsulating an object and a method, and providing functionality to execute the method on the given
 * object using reflection.
 * This record implements the {@link MethodExecutor} interface and serves as a mechanism to invoke methods dynamically.
 */
public record ReflectionExecutor(Object object, Method method)
        implements MethodExecutor {

    @Override
    @NonNull
    public TypedValue execute(@NonNull EvaluationContext context, @NonNull Object target, @NonNull Object... arguments)
            throws AccessException {
        try {
            var value = method.invoke(object, arguments);
            return new TypedValue(value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            var message = "Failed to invoke method %s with arguments [%s] from object %s".formatted(method,
                    Arrays.toString(arguments),
                    object);
            throw new AccessException(message, e);
        }
    }
}
