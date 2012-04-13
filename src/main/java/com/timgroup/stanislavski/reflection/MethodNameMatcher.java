package com.timgroup.stanislavski.reflection;

public final class MethodNameMatcher implements MethodCallPredicate {
    private final String methodName;
    private final Class<?> targetClass;

    public MethodNameMatcher(String methodName, Class<?> targetClass) {
        this.methodName = methodName;
        this.targetClass = targetClass;
    }

    @Override
    public boolean apply(MethodCall methodCall) {
        return methodCall.targetClass().equals(targetClass) && methodCall.name().equals(methodName);
    }
}