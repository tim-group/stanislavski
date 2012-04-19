package com.timgroup.stanislavski.magic;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.timgroup.karg.reference.Getter;

public class JavaBeanPropertyMatcher<T> extends TypeSafeDiagnosingMatcher<T> {

    private final String propertyName;
    private final Getter<T, ?> getter;
    private final Matcher<?> matcher;

    public JavaBeanPropertyMatcher(String propertyName, Getter<T, ?> getter, Matcher<?> matcher) {
        this.propertyName = propertyName;
        this.getter = getter;
        this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[").appendText(propertyName).appendText("] ");
        matcher.describeTo(description);
    }

    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        Object propertyValue = getter.get(item);
        if (matcher.matches(propertyValue)) {
            return true;
        }
        mismatchDescription.appendText("[").appendText(propertyName).appendText("] ");
        matcher.describeMismatch(propertyValue, mismatchDescription);
        return false;
    }
}