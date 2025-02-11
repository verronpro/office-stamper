package pro.verron.officestamper.core;

import org.springframework.expression.MethodExecutor;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;


/// The Invoker class encapsulates an object and a method to be invoked on that object. It provides a way to
/// execute
/// the specified method with given arguments within a certain evaluation context.
public record Invoker(String name, Invokers.Args args, MethodExecutor executor) {

    /// @param obj    the target object on which the method is to be invoked
    /// @param method the method to be invoked on the target object
    public Invoker(Object obj, Method method) {
        this(method.getName(), asList(method.getParameterTypes()), new ReflectionExecutor(obj, method));
    }

    public Invoker(String name, List<Class<?>> args, MethodExecutor executor) {
        this(name, new Invokers.Args(args), executor);
    }
}
