package com.timgroup.stanislavski.magic.matchers;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.timgroup.karg.reference.Getter;
import com.timgroup.karg.reflection.ReflectiveAccessorFactory;
import com.timgroup.stanislavski.interpreters.MethodCallInterpreter;
import com.timgroup.stanislavski.magic.MethodNameToPropertyNameTranslator;
import com.timgroup.stanislavski.reflection.MethodCall;

public class JavaBeanPropertyMatcherMaker<T> implements MethodCallInterpreter<Matcher<? super T>> {

    public static <T> Iterable<Matcher<? super T>> interpret(Class<T> targetClass, Iterable<MethodCall> callHistory) {
        return Iterables.transform(callHistory, forClass(targetClass));
    }
    
    public static <T> JavaBeanPropertyMatcherMaker<T> forAnyClass() {
        return new JavaBeanPropertyMatcherMaker<T>();
    }
    
    public static <T> JavaBeanPropertyMatcherMaker<T> forClass(Class<T> targetClass) {
        return new JavaBeanPropertyMatcherMaker<T>(targetClass);
    }
    
    private final Function<String, Getter<T, ?>> getterProvider;
    
    public JavaBeanPropertyMatcherMaker() {
        this.getterProvider = new Function<String, Getter<T, ?>>() {
            @Override public Getter<T, ?> apply(String arg0) {
                return LateBindingGetter.forPropertyNamed(arg0);
            }
        };
    }
    
    public JavaBeanPropertyMatcherMaker(Class<T> targetClass) {
        final ReflectiveAccessorFactory<T> catalogue = ReflectiveAccessorFactory.forClass(targetClass);
        this.getterProvider = new Function<String, Getter<T, ?>>() {
            @Override public Getter<T, ?> apply(String name) {
                return catalogue.getGetter(name);
            }
        };
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
    
    public <V> Matcher<? super T> make(MethodCall methodCall, Matcher<? super V> matcher) {
        return make(getPropertyName(methodCall), matcher);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <V> Matcher<? super T> make(String propertyName, Matcher<? super V> matcher) {
        Getter<T, V> getter = (Getter) getterProvider.apply(propertyName);
        return JavaBeanPropertyMatcher.matching(propertyName, getter, matcher);
    }
    
    @SuppressWarnings("unchecked")
    private <V> Matcher<? super V> getMatcher(MethodCall methodCall) {
        final V firstArgumentValue = (V) methodCall.firstArgument().value();
        if (firstArgumentValue instanceof Matcher) {
            return (Matcher<? super V>) firstArgumentValue;
        }
        return Matchers.equalTo(firstArgumentValue);
    }

    private String getPropertyName(MethodCall methodCall) {
        return MethodNameToPropertyNameTranslator.interpret(methodCall);
    }
}