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
        for (Object element : branch)
            for (PropertyAccessor accessor : accessors)
                if (accessor.canRead(context, element, name)) return true;
        return false;
    }

    @Override
    public TypedValue read(EvaluationContext context, @Nullable Object target, String name)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) throw new AccessException("Target is not a ContextBranch");

        for (Object element : branch)
            for (PropertyAccessor accessor : accessors)
                if (accessor.canRead(context, element, name)) return accessor.read(context, element, name);
        throw new AccessException("Unable to read property '" + name + "' from any context object");
    }

    @Override
    public boolean canWrite(EvaluationContext context, @Nullable Object target, String name)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) return false;
        for (Object element : branch)
            for (PropertyAccessor accessor : accessors)
                if (accessor.canWrite(context, element, name)) return true;
        return false;
    }

    @Override
    public void write(EvaluationContext context, @Nullable Object target, String name, @Nullable Object newValue)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) throw new AccessException("Target is not a ContextBranch");

        AccessException lastException = null;
        for (Object element : branch) {
            for (PropertyAccessor accessor : accessors) {
                if (!accessor.canWrite(context, element, name)) {continue;}
                try {
                    accessor.write(context, element, name, newValue);
                    return;
                } catch (AccessException e) {
                    lastException = e;
                }
            }
        }

        if (lastException != null) throw lastException;
        throw new AccessException("Unable to write property '%s' to any context object".formatted(name));
    }
}
