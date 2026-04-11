package pro.verron.officestamper.preset;


import org.springframework.expression.*;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.*;
import pro.verron.officestamper.api.EvaluationContextFactory;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.ArrayList;

/// Utility class for configuring the [EvaluationContext] used by officestamper.
public class EvaluationContextFactories {

    private EvaluationContextFactories() {
        throw new OfficeStamperException("EvaluationContextConfigurers cannot be instantiated");
    }

    /// Returns an [EvaluationContextFactory] instance that does no customization.
    /// This factory does nothing to the [StandardEvaluationContext] class, and therefore all the unfiltered features
    /// are accessible. It should be used when there is a need to use the powerful features of the aforementioned class,
    /// and there is a trust that the template won't contain any dangerous injections.
    ///
    /// @return an [EvaluationContextFactory] instance
    public static EvaluationContextFactory noopFactory() {
        return object -> {
            var standardEvaluationContext = new StandardEvaluationContext(object);
            var reflectivePropertyAccessor = new ReflectivePropertyAccessor();
            var mapAccessor = new MapAccessor();
            var propertyAccessors = new ArrayList<PropertyAccessor>();
            propertyAccessors.add(reflectivePropertyAccessor);
            propertyAccessors.add(mapAccessor);
            standardEvaluationContext.setPropertyAccessors(propertyAccessors);
            return standardEvaluationContext;
        };
    }

    /// Returns a default [EvaluationContextFactory] instance.
    /// The default factory provides better default security for the [EvaluationContext] used by OfficeStamper. It
    /// sets up the context with enhanced security measures, such as limited property accessors, constructor resolvers,
    /// and method resolvers. It also sets a type locator, type converter, type comparator, and operator overloader.
    /// This factory is recommended to be used when there is a need for improved security and protection against
    /// potentially dangerous injections in the template.
    ///
    /// @return an [EvaluationContextFactory] instance with enhanced security features
    public static EvaluationContextFactory defaultFactory() {
        return object -> {
            var standardEvaluationContext = new StandardEvaluationContext(object);

            var propertyAccessor = DataBindingPropertyAccessor.forReadWriteAccess();
            var mapAccessor = new MapAccessor();
            var propertyAccessors = new ArrayList<PropertyAccessor>();
            propertyAccessors.add(propertyAccessor);
            propertyAccessors.add(mapAccessor);
            standardEvaluationContext.setPropertyAccessors(propertyAccessors);

            standardEvaluationContext.setConstructorResolvers(new ArrayList<>());

            var instanceMethodInvocation = DataBindingMethodResolver.forInstanceMethodInvocation();
            var methodResolvers = new ArrayList<MethodResolver>();
            methodResolvers.add(instanceMethodInvocation);
            standardEvaluationContext.setMethodResolvers(methodResolvers);

            BeanResolver beanResolver = (_, _) -> {
                throw new AccessException("Bean resolution not supported for security reasons.");
            };
            standardEvaluationContext.setBeanResolver(beanResolver);

            TypeLocator typeLocator = typeName -> {
                throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
            };
            standardEvaluationContext.setTypeLocator(typeLocator);

            standardEvaluationContext.setTypeConverter(new StandardTypeConverter());
            standardEvaluationContext.setTypeComparator(new StandardTypeComparator());
            standardEvaluationContext.setOperatorOverloader(new StandardOperatorOverloader());
            return standardEvaluationContext;
        };
    }

}
