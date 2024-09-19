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
 * The Invoker class encapsulates an object and a method to be invoked on that object. It provides a way to execute
 * the specified method with given arguments within a certain evaluation context.
 *
 * @param object the target object on which the method is to be invoked
 * @param method the method to be invoked on the target object
 */
public record Invoker(Object object, Method method)
        implements MethodExecutor {
    @Override @NonNull
    public TypedValue execute(
            @NonNull EvaluationContext context,
            @NonNull Object target,
            @NonNull Object... arguments
    )
            throws AccessException {
        try {
            var value = method.invoke(object, arguments);
            return new TypedValue(value);
        } catch (InvocationTargetException | IllegalAccessException e) {
            var message = "Failed to invoke method %s with arguments [%s] from object %s"
                    .formatted(method, Arrays.toString(arguments), object);
            throw new AccessException(message, e);
        }
    }
}
