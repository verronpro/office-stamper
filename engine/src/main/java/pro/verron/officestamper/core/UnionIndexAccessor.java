package pro.verron.officestamper.core;

import org.jspecify.annotations.Nullable;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.IndexAccessor;
import org.springframework.expression.TypedValue;

import java.util.List;

record UnionIndexAccessor(List<IndexAccessor> accessors)
        implements IndexAccessor {

    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return new Class[]{ContextBranch.class};
    }

    @Override
    public boolean canRead(EvaluationContext context, Object target, Object index)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) return false;
        for (Object element : branch)
            for (IndexAccessor accessor : accessors)
                if (accessor.canRead(context, element, index)) return true;
        return false;
    }

    @Override
    public TypedValue read(EvaluationContext context, Object target, Object index)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) throw new AccessException("Target is not a ContextBranch");
        for (Object element : branch)
            for (IndexAccessor accessor : accessors)
                if (accessor.canRead(context, element, index)) return accessor.read(context, element, index);
        throw new AccessException("Unable to read index '" + index + "' from any context object");
    }

    @Override
    public boolean canWrite(EvaluationContext context, Object target, Object index)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) return false;
        for (Object element : branch)
            for (IndexAccessor accessor : accessors)
                if (accessor.canWrite(context, element, index)) return true;
        return false;
    }

    @Override
    public void write(EvaluationContext context, @Nullable Object target, Object index, @Nullable Object newValue)
            throws AccessException {
        if (!(target instanceof ContextBranch branch)) throw new AccessException("Target is not a ContextBranch");

        AccessException lastException = null;
        for (Object element : branch) {
            for (IndexAccessor accessor : accessors) {
                if (accessor.canWrite(context, element, index)) {
                    try {
                        accessor.write(context, element, index, newValue);
                        return;
                    } catch (AccessException e) {
                        lastException = e;
                    }
                }
            }
        }
        throw lastException != null
                ? lastException
                : new AccessException("Unable to write index '" + index + "' to any context object");
    }
}
