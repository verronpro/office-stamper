package pro.verron.officestamper.core;

import org.jspecify.annotations.Nullable;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import java.util.List;

record UnionPropertyAccessor(List<PropertyAccessor> accessors)
        implements PropertyAccessor {

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class[]{ContextBranch.class};
    }

    @Override
    public boolean canRead(EvaluationContext context, @Nullable Object target, String name)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) return false;
        for (Object subTarget : branch.list())
            for (PropertyAccessor accessor : accessors)
                if (accessor.canRead(context, subTarget, name)) return true;
        return false;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, String name)
            throws AccessException {
        if (!(target instanceof ContextBranch branch))
            throw new AccessException("Target is not a ContextBranch");

        for (Object subTarget : branch.list()) {
            for (PropertyAccessor accessor : accessors) {
                if (accessor.canRead(context, subTarget, name)) {
                    return accessor.read(context, subTarget, name);
                }
            }
        }
        throw new AccessException("Unable to read property '" + name + "' from any context object");
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) return false;
        for (Object subTarget : branch.list())
            for (PropertyAccessor accessor : accessors)
                if (accessor.canWrite(context, subTarget, name)) return true;
        return false;
    }

    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue)
            throws AccessException {
        if (!(target instanceof ContextBranch branch))
            throw new AccessException("Target is not a ContextBranch");

        AccessException lastException = null;
        for (Object subTarget : branch.list()) {
            for (PropertyAccessor accessor : accessors) {
                if (accessor.canWrite(context, subTarget, name)) {
                    try {
                        accessor.write(context, subTarget, name, newValue);
                        return;
                    } catch (AccessException e) {
                        lastException = e;
                    }
                }
            }
        }
        throw lastException != null
                ? lastException
                : new AccessException("Unable to write property '" + name + "' to any context object");
    }
}
