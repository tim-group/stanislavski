package com.timgroup.stanislavski.magic;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.timgroup.karg.reference.Getter;
import com.timgroup.karg.reflection.AccessorCatalogue;
import com.timgroup.stanislavski.interpreters.MethodCallInterpreter;
import com.timgroup.stanislavski.reflection.MethodCall;

public class JavaBeanPropertyMatcherMaker<T> implements MethodCallInterpreter<Matcher<? super T>> {

    public static <T> Iterable<Matcher<? super T>> interpret(Class<T> targetClass, Iterable<MethodCall> callHistory) {
        return Iterables.transform(callHistory, forClass(targetClass));
    }
    
    public static <T> JavaBeanPropertyMatcherMaker<T> forClass(Class<T> targetClass) {
        return new JavaBeanPropertyMatcherMaker<T>(targetClass);
    }
    
    private final AccessorCatalogue<T> catalogue;
    
    public JavaBeanPropertyMatcherMaker(Class<T> targetClass) {
        this.catalogue = AccessorCatalogue.forClass(targetClass);
    }

    @Override
    public Matcher<? super T> apply(MethodCall methodCall) {
        if (methodCall.hasAnnotation(MatchesWith.class)) {
            return useMatchesWith(methodCall);
        }
        return make(methodCall, getMatcher(methodCall));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Matcher<? super T> useMatchesWith(MethodCall methodCall) {
        try {
            return (Matcher) methodCall.getAnnotation(MatchesWith.class)
                                       .value()
                                       .newInstance()
                                       .apply(methodCall);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Matcher<? super T> make(MethodCall arg0, Matcher<?> matcher) {
        return make(getPropertyName(arg0), matcher);
    }
    
    public Matcher<? super T> make(String propertyName, Matcher<?> matcher) {
        Getter<T, ?> getter = catalogue.getAccessor(propertyName);
        Preconditions.checkNotNull(getter, "No accessible property found with name \"%s\"", propertyName);
        return new JavaBeanPropertyMatcher<T>(propertyName, getter,matcher);
    }
    
    private Matcher<?> getMatcher(MethodCall methodCall) {
        final Object firstArgumentValue = methodCall.firstArgument().value();
        if (firstArgumentValue instanceof Matcher) {
            return (Matcher<?>) firstArgumentValue;
        }
        return Matchers.equalTo(firstArgumentValue);
    }

    private String getPropertyName(MethodCall methodCall) {
        return MethodNameToPropertyNameInterpreter.interpret(methodCall);
    }
}