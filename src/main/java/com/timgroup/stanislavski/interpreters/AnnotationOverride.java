package com.timgroup.stanislavski.interpreters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.timgroup.stanislavski.reflection.MethodCall;

public class AnnotationOverride<A extends Annotation, T> implements MethodCallInterpreter<Optional<T>> {
    
    public static <A extends Annotation, T> Function<MethodCall, Optional<T>> obtainingValueOf(final Class<A> annotationClass) {
        return new AnnotationOverride<A, T>(annotationClass, new Function<A, T>() {
            @SuppressWarnings("unchecked")
            @Override public T apply(A annotation) {
                try {
                    Method method = annotationClass.getMethod("value");
                    return (T) method.invoke(annotation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private final Class<A> annotationClass;
    private final Function<A, T> annotationInterpreter;
    
    public AnnotationOverride(Class<A> annotationClass, Function<A, T> annotationInterpreter) {
        this.annotationClass = annotationClass;
        this.annotationInterpreter = annotationInterpreter;
    }
    
    @Override
    public Optional<T> apply(MethodCall methodCall) {
        if (!methodCall.hasAnnotation(annotationClass)) {
            return Optional.absent();
        }
        A annotation = methodCall.getAnnotation(annotationClass);
        return Optional.of(annotationInterpreter.apply(annotation));
    }

}
