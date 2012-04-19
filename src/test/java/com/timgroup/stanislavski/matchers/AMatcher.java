package com.timgroup.stanislavski.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class AMatcher<T> extends TypeSafeDiagnosingMatcher<Matcher<T>> {
    private final T instance;
    private final boolean shouldMatch;
    private Matcher<? super String> descriptionMatcher;
    private Matcher<? super String> mismatchDescriptionMatcher;

    public static <T> AMatcher<T> that_matches(T instance) {
        return new AMatcher<T>(instance, true);
    }
    
    public static <T> AMatcher<T> that_fails_to_match(T instance) {
        return new AMatcher<T>(instance, false);
    }
    
    private AMatcher(T instance, boolean shouldMatch) {
        this.instance = instance;
        this.shouldMatch = shouldMatch;
    }
    
    public AMatcher<T> with_the_description(String description) {
        return with_the_description(Matchers.equalTo(description));
    }
    
    public AMatcher<T> with_the_description(Matcher<? super String> descriptionMatcher) {
        this.descriptionMatcher = descriptionMatcher;
        return this;
    }
    
    public AMatcher<T> with_the_mismatch_description(String mismatchDescription) {
        return with_the_mismatch_description(Matchers.equalTo(mismatchDescription));
    }
    
    public AMatcher<T> with_the_mismatch_description(Matcher<? super String> mismatchDescriptionMatcher) {
        this.mismatchDescriptionMatcher = mismatchDescriptionMatcher;
        return this;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("A matcher for a ").appendText(instance.getClass().getSimpleName()).appendText( " that should ");
        if (!shouldMatch) {
            description.appendText(" fail to ");
        }
        description.appendText("match the instance ").appendValue(instance);
        if (descriptionMatcher != null) {
            description.appendText(" with a description ");
            descriptionMatcher.describeTo(description);
        }
        if (mismatchDescriptionMatcher != null) {
            description.appendText(" with a mismatch description ");
            mismatchDescriptionMatcher.describeTo(description);
        }
    }

    @Override
    protected boolean matchesSafely(Matcher<T> item,
            Description mismatchDescription) {
        boolean matched = item.matches(instance);
        if (matched!=shouldMatch) {
            mismatchDescription.appendText(shouldMatch ? "failed to match " : "matched ").appendText("the instance");
            if (shouldMatch) {
                mismatchDescription.appendText(" and gave the mismatch description ").appendValue(getMismatchDescription(item));
            }
            return false;
        }
        
        if (shouldMatch && descriptionMatcher!=null) {
            return givesExpected("description", descriptionMatcher, getDescription(item), mismatchDescription);
        }
        
        if (!shouldMatch && mismatchDescriptionMatcher!=null) {
            return givesExpected( "mismatch description", mismatchDescriptionMatcher, getMismatchDescription(item), mismatchDescription);
        }
        return true;
    }
    
    private boolean givesExpected(String descriptionType,
                                  Matcher<? super String> matcher,
                                  String description,
                                  Description mismatchDescription) {
        if (matcher.matches(description)) {
            return true;
        }
        mismatchDescription.appendText(" gave a ").appendText(descriptionType).appendText(" ");
        matcher.describeMismatch(description, mismatchDescription);
        return false;
    }
    
    private String getDescription(Matcher<T> item) {
        StringDescription description = new StringDescription();
        item.describeTo(description);
        return description.toString();
    }
    
    private String getMismatchDescription(Matcher<T> item) {
        StringDescription description = new StringDescription();
        item.describeMismatch(instance, description);
        return description.toString();
    }
}