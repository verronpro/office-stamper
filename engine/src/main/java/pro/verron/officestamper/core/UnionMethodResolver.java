package pro.verron.officestamper.core;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;

import java.util.List;

record UnionMethodResolver(List<MethodResolver> resolvers)
        implements MethodResolver {
    private static final Logger log = LoggerFactory.getLogger(UnionMethodResolver.class);

    @Override
    @Nullable
    public MethodExecutor resolve(
            EvaluationContext context,
            Object target,
            String name,
            List<TypeDescriptor> argumentTypes
    ) {
        if (!(target instanceof ContextBranch branch)) return null;

        for (Object subTarget : branch.list()) {
            for (MethodResolver resolver : resolvers) {
                try {
                    var executor = resolver.resolve(context, subTarget, name, argumentTypes);
                    if (executor != null) return executor;
                } catch (AccessException e) {
                    log.atInfo()
                       .setCause(e)
                       .log("AccessException while resolving sub-target '{}' for method '{}'."
                            + " Continue to next target.", subTarget, name);
                }
            }
        }
        return null;
    }
}
