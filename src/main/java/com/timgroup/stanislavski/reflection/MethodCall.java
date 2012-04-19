package com.timgroup.stanislavski.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;

public class MethodCall {
    private final Method method;
    private final Object[] rawArgs;
    private final List<MethodCallArgument<?>> arguments;
    
    public static MethodCall create(Method method, Object...args) {
        return new MethodCall(method, args);
    }

    private MethodCall(Method method, Object[] rawArgs) {
        this.method = method;
        this.rawArgs = rawArgs;
        this.arguments = MethodCallArgument.wrapArguments(method, rawArgs);
    }

    public Class<?> targetClass() {
        return method.getDeclaringClass();
    }
    
    public Class<?> returnType() {
        return method.getReturnType();
    }

    public String name() {
        return method.getName();
    }

    public List<MethodCallArgument<?>> arguments() {
        return arguments;
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return method.isAnnotationPresent(annotationClass);
    }
    
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public MethodCallArgument<?> firstArgument() {
        return arguments.get(0);
    }
    
    public Object applyTo(Object target) throws Throwable {
        try {
            return this.method.invoke(target, rawArgs);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    @Override public String toString() {
        return String.format("%s: %s", method, arguments);
    }

    public List<Object> argumentValues() {
        return Lists.transform(arguments, MethodCallArgument.TO_VALUE);
    }
}