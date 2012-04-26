package com.timgroup.stanislavski.magic.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class InstanceWithPropertiesMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
    private final Class<T> targetClass;
    private final Matcher<T> propertiesMatcher;

    public InstanceWithPropertiesMatcher(Class<T> targetClass, Matcher<T> propertiesMatcher) {
        this.targetClass = targetClass;
        this.propertiesMatcher = propertiesMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A ").appendText(targetClass.getSimpleName()).appendText(" ");
        propertiesMatcher.describeTo(description);
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
        
        if (propertiesMatcher.matches(item)) {
            return true;
        }
        
        mismatchDescription.appendText("A ").appendText(targetClass.getSimpleName()).appendText(" ");
        propertiesMatcher.describeMismatch(item, mismatchDescription);
        return false;
    }
}