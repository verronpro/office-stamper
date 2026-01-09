package pro.verron.officestamper.core;

import org.jspecify.annotations.Nullable;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.TypedValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/// A record encapsulating an object and a method, and providing functionality to execute the method on the given object
/// using reflection. This record implements the [MethodExecutor] interface and serves as a mechanism to invoke methods
/// dynamically.
///
/// @param object the object on which to invoke the method.
/// @param method the method to invoke.
public record ReflectionExecutor(Object object, Method method)
        implements MethodExecutor {

    /// Executes the provided method on the given object using the specified arguments. This method utilizes reflection
    /// to invoke the target method dynamically.
    ///
    /// @param context   the evaluation context in which this execution occurs.
    /// @param target    the target object on which the method should be invoked.
    /// @param arguments the arguments to be passed to the method during invocation.
    /// @return a TypedValue wrapping the result of the invoked method.
    /// @throws AccessException if the method cannot be accessed or invoked, or if an error occurs during
    ///                         invocation.
    @Override
    public TypedValue execute(EvaluationContext context, Object target, @Nullable Object... arguments)
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
