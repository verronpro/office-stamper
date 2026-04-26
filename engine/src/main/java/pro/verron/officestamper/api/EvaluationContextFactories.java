package pro.verron.officestamper.api;

import org.springframework.expression.*;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.*;

import java.util.ArrayList;

/// A deprecated class providing factory methods for creating predefined [EvaluationContextFactory] instances.
/// The factories are used to create [EvaluationContext] objects with specific configurations for evaluation logic.
/// This class has been marked for removal in future versions and should no longer be used, it is planned to move this
/// in the core package.
@Deprecated(since = "3.4", forRemoval = true) public class EvaluationContextFactories {

    private EvaluationContextFactories() {
        throw new OfficeStamperException("EvaluationContextFactories cannot be instantiated");
    }

    /// Creates a restricted [EvaluationContextFactory] that provides a predefined,
    /// limited configuration to ensure security and control during expression evaluation.
    /// This factory configures the resulting [EvaluationContext] to disallow certain
    /// operations, such as bean resolution and type resolution, which could pose security risks.
    ///
    /// The resulting context supports access to properties and maps but restricts the
    /// use of reflection for method invocation and disables constructor resolution. The factory
    /// is particularly suited for scenarios where strict evaluation constraints are necessary
    /// to avoid potential misuse or unintended consequences.
    ///
    /// @return a restricted [EvaluationContextFactory] with controlled behavior and
    ///         limited capabilities for evaluating expressions.
    /// @deprecated since 3.4, for removal in a future version. The use of this factory is no longer
    ///             recommended due to upcoming changes in evaluation context configurations.
    @Deprecated(since = "3.4", forRemoval = true)
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
    /// for reflective property access and map-based property access. It incorporates
    /// an ordered list of [PropertyAccessor] implementations, granting enhanced
    /// capabilities for resolving properties and accessing data in expressions.
    ///
    /// The resulting context is designed for scenarios where relaxed evaluation
    /// constraints are needed to allow dynamic property and map access within the
    /// evaluation process. It may not be suitable for use in security-sensitive
    /// contexts due to its permissive nature.
    ///
    /// @return a permissive [EvaluationContextFactory] configured for flexible property
    ///         and map access during evaluation.
    /// @deprecated since 3.4, for removal in a future version. The use of this factory is no longer
    ///             recommended due to upcoming changes in evaluation context configurations.
    @Deprecated(since = "3.4", forRemoval = true)
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
