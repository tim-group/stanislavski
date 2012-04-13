package com.timgroup.stanislavski.matchers;

import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.google.common.collect.Lists;
import com.timgroup.stanislavski.reflection.MethodCall;
import com.timgroup.stanislavski.reflection.MethodCallArgument;

public final class AMethodCall<C> extends TypeSafeDiagnosingMatcher<MethodCall> {
    private final String methodName;
    private final Class<C> targetClass;
    private final Collection<AnArgument<?>> arguments = Lists.newLinkedList();
    
    public static interface MethodNameBinder {
        <C> AMethodCall<C> of(Class<C> interfaceClass);
    }
    
    public static AMethodCall.MethodNameBinder to(final String methodName) {
        return new MethodNameBinder() {
            @Override public <C> AMethodCall<C> of(Class<C> targetClass) { return new AMethodCall<C>(methodName, targetClass); }
        };
    }

    private AMethodCall(String methodName, Class<C> targetClass) {
        this.methodName = methodName;
        this.targetClass = targetClass;
    }
    
    public AMethodCall<C> with(AnArgument<?> anArgument) {
        arguments.add(anArgument);
        return this;
    }
    
    public AMethodCall<C> and(AnArgument<?> anArgument) {
        arguments.add(anArgument);
        return this;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText(" a method call to the method ").appendValue(methodName).appendText(" on class ").appendValue(targetClass);
        if (arguments.size() > 0) {
            description.appendText(" with arguments ").appendValueList("[", ",", "]", arguments);
        }
    }

    @Override
    protected boolean matchesSafely(MethodCall item, Description mismatchDescription) {
        if (!item.targetClass().equals(targetClass)) {
            mismatchDescription.appendText(" the target class was ").appendValue(targetClass);
            return false;
        }
        if (!item.name().equals(methodName)) {
            mismatchDescription.appendText(" the method called was ").appendText(methodName);
            return false;
        }
        if (arguments.size() != item.arguments().size()) {
            mismatchDescription.appendText(" the number of arguments was ").appendValue(item.arguments().size());
            return false;
        }
        Iterator<AnArgument<?>> matcherIterator = arguments.iterator();
        Iterator<MethodCallArgument<?>> argumentIterator = item.arguments().iterator();
        int argumentIndex = 0;
        while (matcherIterator.hasNext()) {
            AnArgument<?> matcher = matcherIterator.next();
            MethodCallArgument<?> argument = argumentIterator.next();
            argumentIndex += 1;
            if (!matcher.matches(argument)) {
                mismatchDescription.appendText(" argument [").appendValue(argumentIndex).appendText("]");
                matcher.describeMismatch(argument, mismatchDescription);
                return false;
            }
        }
        return true;
    }
}