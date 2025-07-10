package pro.verron.officestamper.core;

import org.springframework.expression.MethodExecutor;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;


/**
 * Represents an invoker that encapsulates the name, arguments, and executor of a method
 * to be invoked.
 * It acts as a wrapper to facilitate method invocation with specific parameters,
 * and a dedicated execution strategy.
 */
public record Invoker(String name, Invokers.Args args, MethodExecutor executor) {

    /**
     * Constructs an {@code Invoker} instance by extracting the method name, parameter types,
     * and creating a corresponding {@link ReflectionExecutor} for the provided object and method.
     *
     * @param obj    the object on which the method will be invoked.
     * @param method the method to be invoked, including its name and parameter types.
     */
    public Invoker(Object obj, Method method) {
        this(method.getName(), asList(method.getParameterTypes()), new ReflectionExecutor(obj, method));
    }

    /**
     * Constructs an {@code Invoker} instance using the provided method name, argument types, and executor.
     *
     * @param name     the name of the method to be invoked.
     * @param args     the list of argument types required for the method invocation.
     * @param executor the executor responsible for executing the method.
     */
    public Invoker(String name, List<Class<?>> args, MethodExecutor executor) {
        this(name, new Invokers.Args(args), executor);
    }
}
