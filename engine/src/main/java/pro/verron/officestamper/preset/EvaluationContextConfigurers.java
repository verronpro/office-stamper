package pro.verron.officestamper.preset;


import org.springframework.expression.EvaluationContext;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.support.*;
import pro.verron.officestamper.api.EvaluationContextConfigurer;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// Utility class for configuring the EvaluationContext used by officestamper.
public class EvaluationContextConfigurers {

    private EvaluationContextConfigurers() {
        throw new OfficeStamperException("EvaluationContextConfigurers cannot be instantiated");
    }

    /// Returns a [EvaluationContextConfigurer] instance that does no customization.
    ///
    /// This configurer does nothing to the StandardEvaluationContext class, and therefore all the
    /// unfiltered features are accessible.
    /// It should be used when there is a need to use the
    /// powerful features of the aforementioned class, and there is a trust that the template won't
    /// contain any dangerous injections.
    ///
    /// @return a [EvaluationContextConfigurer] instance
    public static EvaluationContextConfigurer noopConfigurer() {
        return new NoOpEvaluationContextConfigurer();
    }

    /// Returns a default [EvaluationContextConfigurer] instance.
    ///
    /// The default configurer provides better default security for the
    /// [EvaluationContext] used by OfficeStamper.
    /// It sets up the context with enhanced security measures, such as
    /// limited property accessors, constructor resolvers, and method resolvers.
    /// It also sets a type locator, type converter, type comparator, and operator overloader.
    /// This configurer is recommended to be used when there is a need for improved security
    /// and protection against potential dangerous injections in the template.
    ///
    /// @return a [EvaluationContextConfigurer] instance with enhanced security features
    public static EvaluationContextConfigurer defaultConfigurer() {
        return new DefaultEvaluationContextConfigurer();
    }

    /// [EvaluationContextConfigurer] that does no customization.
    ///
    /// The NoOpEvaluationContextConfigurer is a configuration placeholder used to indicate the
    /// intention to keep the standard powerful features provided by the
    /// Spring framework's StandardEvaluationContext class.
    ///
    /// StandardEvaluationContext is a powerful class by default, which can lead to potential security risks
    /// if not properly managed. This might include potential dangerous injections in the template.
    ///
    /// This configurer does nothing to the StandardEvaluationContext class, and therefore all the
    /// unfiltered features are accessible. It should be used when there is a need to use the
    /// powerful features of the aforementioned class, and there is a trust that the template won't
    /// contain any dangerous injections.
    ///
    /// @author Joseph Verron
    /// @author Mario Siegenthaler
    /// @version ${version}
    /// @since 1.0.13
    private static class NoOpEvaluationContextConfigurer
            implements EvaluationContextConfigurer {

        /// Configures the provided StandardEvaluationContext.
        ///
        /// @param context the StandardEvaluationContext to be configured, not null
        @Override
        public void configureEvaluationContext(StandardEvaluationContext context) {
            // Just add the MapAccessor to the standard list.
            context.setPropertyAccessors(List.of(new ReflectivePropertyAccessor(), new MapAccessor()));
        }
    }

    /// [EvaluationContextConfigurer] that has better default security,
    /// especially doesn't allow especially known injections.
    ///
    /// @author Joseph Verron
    /// @version ${version}
    /// @since 1.6.5
    private static class DefaultEvaluationContextConfigurer
            implements EvaluationContextConfigurer {

        @Override
        public void configureEvaluationContext(StandardEvaluationContext context) {
            TypeLocator typeLocator = typeName -> {
                throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
            };
            context.setPropertyAccessors(List.of(DataBindingPropertyAccessor.forReadWriteAccess(), new MapAccessor()));
            context.setConstructorResolvers(Collections.emptyList());
            context.setMethodResolvers(new ArrayList<>(List.of(DataBindingMethodResolver.forInstanceMethodInvocation())));
            //noinspection DataFlowIssue, ignore the warning since it is a workaround fixing potential security issues
            context.setBeanResolver(null);
            context.setTypeLocator(typeLocator);
            context.setTypeConverter(new StandardTypeConverter());
            context.setTypeComparator(new StandardTypeComparator());
            context.setOperatorOverloader(new StandardOperatorOverloader());
        }
    }
}
