package com.timgroup.stanislavski.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.timgroup.stanislavski.reflection.MethodCallArgument;

public final class AnArgument<C> extends TypeSafeDiagnosingMatcher<MethodCallArgument<C>> {
    private final C value;

    public static <C> AnArgument<C> of(C value) {
        return new AnArgument<C>(value);
    }
    
    private AnArgument(C value) {
        this.value = value;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText(" an argument with the value ").appendValue(value);
    }

    @Override
    protected boolean matchesSafely(MethodCallArgument<C> item, Description mismatchDescription) {
        if (item.value().equals(value)) {
            return true;
        }
        mismatchDescription.appendText(" had the value ").appendValue(value);
        return false;
    }
    
}