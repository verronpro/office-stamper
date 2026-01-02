package pro.verron.officestamper.preset;


import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.*;
import pro.verron.officestamper.api.EvaluationContextFactory;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

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
            // Just add the MapAccessor to the standard list.
            standardEvaluationContext.setPropertyAccessors(List.of(new ReflectivePropertyAccessor(),
                    new MapAccessor()));
            return standardEvaluationContext;
        };
    }

    /// Returns a default [EvaluationContextFactory] instance.
    /// The default factory provides better default security for the [EvaluationContext] used by OfficeStamper. It
    /// sets up the context with enhanced security measures, such as limited property accessors, constructor resolvers,
    /// and method resolvers. It also sets a type locator, type converter, type comparator, and operator overloader.
    /// This factory is recommended to be used when there is a need for improved security and protection against
    /// potential dangerous injections in the template.
    ///
    /// @return an [EvaluationContextFactory] instance with enhanced security features
    public static EvaluationContextFactory defaultFactory() {
        return object -> {
            var standardEvaluationContext = new StandardEvaluationContext(object);
            TypeLocator typeLocator = typeName -> {
                throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
            };
            standardEvaluationContext.setPropertyAccessors(List.of(DataBindingPropertyAccessor.forReadWriteAccess(),
                    new MapAccessor()));
            standardEvaluationContext.setConstructorResolvers(emptyList());
            standardEvaluationContext.setMethodResolvers(new ArrayList<>(List.of(DataBindingMethodResolver.forInstanceMethodInvocation())));
            standardEvaluationContext.setBeanResolver((_, _) -> {
                throw new AccessException("Bean resolution not supported for security reasons.");
            });
            standardEvaluationContext.setTypeLocator(typeLocator);
            standardEvaluationContext.setTypeConverter(new StandardTypeConverter());
            standardEvaluationContext.setTypeComparator(new StandardTypeComparator());
            standardEvaluationContext.setOperatorOverloader(new StandardOperatorOverloader());
            return standardEvaluationContext;
        };
    }

}
