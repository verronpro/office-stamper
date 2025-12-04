package pro.verron.officestamper.core;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.lang.Nullable;

import java.util.List;

record UnionMethodResolver(List<MethodResolver> resolvers)
        implements MethodResolver {
    @Override
    @Nullable
    public MethodExecutor resolve(
            EvaluationContext context,
            Object target,
            String name,
            List<TypeDescriptor> argumentTypes
    )
            throws AccessException {
        if (!(target instanceof ContextBranch branch))
            return null;

        for (Object subTarget : branch.list()) {
            for (MethodResolver resolver : resolvers) {
                try {
                    var executor = resolver.resolve(context, subTarget, name, argumentTypes);
                    if (executor != null) return executor;
                } catch (AccessException _) {}
            }
        }
        return null;
    }
}
