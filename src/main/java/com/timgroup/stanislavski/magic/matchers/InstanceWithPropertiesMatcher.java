package com.timgroup.stanislavski.magic.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class InstanceWithPropertiesMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
    private final Class<T> targetClass;
    private final Iterable<Matcher<? super T>> propertyMatchers;

    public InstanceWithPropertiesMatcher(Class<T> targetClass, Iterable<Matcher<? super T>> propertyMatchers) {
        this.targetClass = targetClass;
        this.propertyMatchers = propertyMatchers;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A ").appendText(targetClass.getSimpleName()).appendText(" ");
        boolean first = true;
        for (Matcher<? super T> propertyMatcher : propertyMatchers) {
            if (first) {
                first = false;
            } else {
                description.appendText(" and ");
            }
            propertyMatcher.describeTo(description);
        }
    }

    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        if (item==null) {
            mismatchDescription.appendText("was null");
            return false;
        }
        
        if (!targetClass.isAssignableFrom(item.getClass())) {
            mismatchDescription.appendText("was not a ").appendValue(targetClass);
            return false;
        }
        
        if (Matchers.allOf(propertyMatchers).matches(item)) {
            return true;
        }
        
        mismatchDescription.appendText("was a ").appendText(targetClass.getSimpleName()).appendText(" ");
        boolean first = true;
        for (Matcher<? super T> propertyMatcher : propertyMatchers) {
            if (first) {
                first = false;
            } else {
                mismatchDescription.appendText(" and ");
            }
            propertyMatcher.describeMismatch(item, mismatchDescription);
        }
        return false;
    }
}