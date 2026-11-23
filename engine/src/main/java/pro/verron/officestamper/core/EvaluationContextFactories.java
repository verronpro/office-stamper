package pro.verron.officestamper.core;

import org.springframework.expression.AccessException;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.DataBindingMethodResolver;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.MapAccessor;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardOperatorOverloader;
import org.springframework.expression.spel.support.StandardTypeComparator;
import org.springframework.expression.spel.support.StandardTypeConverter;
import pro.verron.officestamper.api.EvaluationContextFactory;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.ArrayList;

/// Internal holder of the predefined [EvaluationContextFactory] instances backing the
/// SpEL [SecurityMode][pro.verron.officestamper.api.SecurityMode]. This package has
/// qualified JPMS exports (visible only to the provider modules), so this class is not
/// part of the public API: user code selects one of these factories through
/// [pro.verron.officestamper.api.OfficeStamperConfiguration#setSpelSecurityMode(SecurityMode)]
/// and can supply a custom factory through
/// [pro.verron.officestamper.api.OfficeStamperConfiguration#setEvaluationContextFactory(EvaluationContextFactory)].
public final class EvaluationContextFactories {

    private EvaluationContextFactories() {
        throw new OfficeStamperException("EvaluationContextFactories cannot be instantiated");
    }

    /// Creates a restricted [EvaluationContextFactory] that provides a predefined,
    /// limited configuration to ensure security and control during expression evaluation.
    /// This factory configures the resulting [EvaluationContext] to disallow certain
    /// operations, such as bean resolution and type resolution, which could pose security risks.
    ///
    /// The resulting context supports access to properties and maps but restricts the
    /// use of reflection for method invocation and disables constructor resolution.
    ///
    /// @return a restricted [EvaluationContextFactory] with controlled behavior
    public static EvaluationContextFactory restrictedFactory() {
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

    /// Creates a permissive [EvaluationContextFactory] that provides a configuration
    /// allowing flexible property and map access during evaluation.
    ///
    /// This factory configures the resulting [EvaluationContext] to include support
    /// for reflective property access and map-based property access.
    ///
    /// The resulting context is designed for scenarios where relaxed evaluation
    /// constraints are needed. It may not be suitable for use in security-sensitive
    /// contexts due to its permissive nature.
    ///
    /// @return a permissive [EvaluationContextFactory] configured for flexible property
    ///         and map access
    public static EvaluationContextFactory permissiveFactory() {
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
}
