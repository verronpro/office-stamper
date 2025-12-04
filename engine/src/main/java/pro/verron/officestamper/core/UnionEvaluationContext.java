package pro.verron.officestamper.core;

import org.springframework.expression.*;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

record UnionEvaluationContext(EvaluationContext evaluationContext)
        implements EvaluationContext {

    @Override
    public TypedValue getRootObject() {
        return evaluationContext.getRootObject();
    }

    @Override
    public List<PropertyAccessor> getPropertyAccessors() {
        var accessors = evaluationContext.getPropertyAccessors();
        var unionAccessors = new ArrayList<>(accessors);
        unionAccessors.addFirst(new UnionPropertyAccessor(accessors));
        return unionAccessors;
    }

    @Override
    public List<IndexAccessor> getIndexAccessors() {
        var accessors = evaluationContext.getIndexAccessors();
        var unionAccessors = new ArrayList<>(accessors);
        unionAccessors.addFirst(new UnionIndexAccessor(accessors));
        return unionAccessors;
    }

    @Override
    public List<ConstructorResolver> getConstructorResolvers() {
        return evaluationContext.getConstructorResolvers();
    }

    @Override
    public List<MethodResolver> getMethodResolvers() {
        var resolvers = evaluationContext.getMethodResolvers();
        var unionResolvers = new ArrayList<>(resolvers);
        unionResolvers.addFirst(new UnionMethodResolver(resolvers));
        return unionResolvers;
    }

    @Override
    @Nullable
    public BeanResolver getBeanResolver() {
        return evaluationContext.getBeanResolver();
    }

    @Override
    public TypeLocator getTypeLocator() {
        return evaluationContext.getTypeLocator();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return evaluationContext.getTypeConverter();
    }

    @Override
    public TypeComparator getTypeComparator() {
        return evaluationContext.getTypeComparator();
    }

    @Override
    public OperatorOverloader getOperatorOverloader() {
        return evaluationContext.getOperatorOverloader();
    }

    @Override
    public void setVariable(String name, @Nullable Object value) {
        evaluationContext.setVariable(name, value);
    }

    @Override
    @Nullable
    public Object lookupVariable(String name) {
        return evaluationContext.lookupVariable(name);
    }

}
