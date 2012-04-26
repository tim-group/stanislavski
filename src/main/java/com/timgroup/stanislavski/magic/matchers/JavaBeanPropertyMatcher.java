package com.timgroup.stanislavski.magic.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.timgroup.karg.reference.Getter;

public class JavaBeanPropertyMatcher<T, V> extends TypeSafeDiagnosingMatcher<T> {

    public static <T, V> JavaBeanPropertyMatcher<T, V> matching(String propertyName, Getter<T, V> getter, Matcher<? super V> matcher) {
        return new JavaBeanPropertyMatcher<T, V>(propertyName, getter, matcher);
    }
    
    private final String propertyName;
    private final Getter<T, V> getter;
    private final Matcher<? super V> matcher;

    public JavaBeanPropertyMatcher(String propertyName, Getter<T, V> getter, Matcher<? super V> matcher) {
        this.propertyName = propertyName;
        this.getter = getter;
        this.matcher = matcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("whose ").appendText(propertyName).appendText(" ");
        matcher.describeTo(description);
    }

    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        Object propertyValue;
        try {
            propertyValue = getter.get(item);
        } catch (Throwable t) {
            mismatchDescription.appendText("has no accessible property named").appendValue(propertyName);
            return false;
        }
        
        if (matcher.matches(propertyValue)) {
            return true;
        }
        
        mismatchDescription.appendText("whose ").appendText(propertyName).appendText(" ");
        matcher.describeMismatch(propertyValue, mismatchDescription);
        return false;
    }
}