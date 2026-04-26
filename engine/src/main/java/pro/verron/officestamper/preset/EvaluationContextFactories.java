package pro.verron.officestamper.preset;


import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import pro.verron.officestamper.api.EvaluationContextFactory;
import pro.verron.officestamper.api.OfficeStamperException;

/// Utility class for configuring the [EvaluationContext] used by officestamper.
/// @deprecated since 3.4, for removal in a future version. The use of this class is no longer recommended.
@Deprecated(since = "3.4", forRemoval = true) public class EvaluationContextFactories {

    private EvaluationContextFactories() {
        throw new OfficeStamperException("EvaluationContextConfigurers cannot be instantiated");
    }

    /// Returns an [EvaluationContextFactory] instance that does no customization.
    /// This factory does nothing to the [StandardEvaluationContext] class, and therefore all the unfiltered features
    /// are accessible. It should be used when there is a need to use the powerful features of the aforementioned class,
    /// and there is a trust that the template won't contain any dangerous injections.
    ///
    /// @return an [EvaluationContextFactory] instance
    /// @deprecated since 3.4, for removal in a future version. The use of this factory is no longer recommended.
    @Deprecated(since = "3.4", forRemoval = true)
    public static EvaluationContextFactory noopFactory() {
        return pro.verron.officestamper.api.EvaluationContextFactories.permissiveFactory();
    }

    /// Returns a default [EvaluationContextFactory] instance.
    /// The default factory provides better default security for the [EvaluationContext] used by OfficeStamper. It
    /// sets up the context with enhanced security measures, such as limited property accessors, constructor resolvers,
    /// and method resolvers. It also sets a type locator, type converter, type comparator, and operator overloader.
    /// This factory is recommended to be used when there is a need for improved security and protection against
    /// potentially dangerous injections in the template.
    ///
    /// @return an [EvaluationContextFactory] instance with enhanced security features
    /// @deprecated since 3.4, for removal in a future version. The use of this factory is no longer recommended.
    @Deprecated(since = "3.4", forRemoval = true)
    public static EvaluationContextFactory defaultFactory() {
        return pro.verron.officestamper.api.EvaluationContextFactories.restrictedFactory();
    }

}
