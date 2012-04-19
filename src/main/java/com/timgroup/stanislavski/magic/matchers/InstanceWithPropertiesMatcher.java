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
        description.appendText("A ").appendText(targetClass.getSimpleName()).appendText(" with ");
        propertiesMatcher.describeTo(description);
    }

    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        if (item==null) {
            mismatchDescription.appendText("was null");
            return false;
        }
        if (propertiesMatcher.matches(item)) {
            return true;
        }
        
        propertiesMatcher.describeMismatch(item, mismatchDescription);
        return false;
    }
}