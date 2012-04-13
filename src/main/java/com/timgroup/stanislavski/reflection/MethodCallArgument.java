package com.timgroup.stanislavski.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;

public final class MethodCallArgument<F> {

    public static List<MethodCallArgument<?>> wrapArguments(Method method, Object[] args) {
        if (args == null) {
            return Collections.emptyList();
        }
        Builder<MethodCallArgument<?>> builder = ImmutableList.builder();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i=0; i<args.length; i++) {
            MethodCallArgument<?> argument = create(args[i], parameterAnnotations[i]);
            builder.add(argument);
        }
        return builder.build();
    }
    
    private final F value;
    private final Class<F> type;
    private List<Annotation> parameterAnnotations;

    @SuppressWarnings("unchecked")
    public static <F> MethodCallArgument<F> create(F argument, Annotation...parameterAnnotations) {
        List<Annotation> parameterAnnotationList = Lists.newArrayList(parameterAnnotations);
        return new MethodCallArgument<F>(argument, (Class<F>) argument.getClass(), parameterAnnotationList);
    }
    
    private MethodCallArgument(F value, Class<F> type, List<Annotation> parameterAnnotations) {
        this.value = value;
        this.type = type;
        this.parameterAnnotations = parameterAnnotations;
    }

    public Class<F> type() {
        return type;
    }

    public F value() {
        return value;
    }
    
    public List<Annotation> parameterAnnotations() {
        return parameterAnnotations;
    }
    
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return Iterables.any(parameterAnnotations, Predicates.instanceOf(annotationClass));
    }
    
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return (A) Iterables.find(parameterAnnotations, Predicates.instanceOf(annotationClass));
    }
    
    @Override public String toString() {
        return String.format("A %s of %s, annotated with %s", type, value, parameterAnnotations);
    }
}