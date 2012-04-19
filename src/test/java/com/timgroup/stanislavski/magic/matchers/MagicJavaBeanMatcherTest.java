package com.timgroup.stanislavski.magic.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.timgroup.stanislavski.interpreters.Alias;
import com.timgroup.stanislavski.interpreters.MethodCallInterpreter;
import com.timgroup.stanislavski.matchers.AMatcher;
import com.timgroup.stanislavski.reflection.MethodCall;

public class MagicJavaBeanMatcherTest {

    public static final class Person {
        public final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public int getAge() {
            return age;
        }
    }
    
    public static interface PersonMatcher extends Matcher<Person> {
        public PersonMatcher withName(String name);
        public PersonMatcher with_the_name_of(String name);
        public PersonMatcher withName(Matcher<? super String> nameMatcher);
        
        @Alias("name") @MatchesWith(ForenameSurnameMatcher.class)
        public PersonMatcher named(String forename, String surname);
        @Alias("age") public PersonMatcher aged(int age);
        @Alias("age") public PersonMatcher aged(Matcher<? super Integer> ageMatcher);
    }
    
    public static final class ForenameSurnameMatcher implements MethodCallInterpreter<Matcher<?>> {
        @Override
        public Matcher<?> apply(MethodCall methodCall) {
            String fullName = Joiner.on(" ").join(methodCall.argumentValues());
            return JavaBeanPropertyMatcherMaker.forAnyClass()
                                               .make(methodCall, Matchers.equalTo(fullName));
        }
    }
    
    private final Person person = new Person("Julius Caesar", 42);
    private final PersonMatcher matcher = MagicJavaBeanMatcher.matching(Person.class)
                                                              .using(PersonMatcher.class);
    
    @Test public void
    matches_a_property_identified_by_the_method_name() {
        assertThat(matcher.withName(equalTo("Julius Caesar")),
                   AMatcher.that_matches(person)
                           .with_the_description("A Person with ([name] \"Julius Caesar\")"));
    }
    
    @Test public void
    supports_underscore_formatted_method_names() {
        assertThat(matcher.with_the_name_of("Julius Caesar"),
                   AMatcher.that_matches(person)
                           .with_the_description("A Person with ([name] \"Julius Caesar\")"));
    }
    @Test public void
    converts_literal_parameters_into_matchers() {
        assertThat(matcher.withName("Julius Caesar"),
                   AMatcher.that_matches(person)
                           .with_the_description("A Person with ([name] \"Julius Caesar\")"));
    }
    
    @Test public void
    can_override_a_method_name_with_an_alias() {
        assertThat(matcher.aged(42),
                   AMatcher.that_matches(person)
                           .with_the_description("A Person with ([age] <42>)"));
    }
    
    @Test public void
    supports_special_cases_using_the_matches_with_annotation() {
        assertThat(matcher.named("Julius", "Caesar"),
                AMatcher.that_matches(person)
                        .with_the_description("A Person with ([name] \"Julius Caesar\")"));
    }
    
    @Test public void
    gives_a_meaningful_mismatch_description() {
        assertThat(matcher.named("Tiberius", "Caesar"),
                AMatcher.that_fails_to_match(person)
                        .with_the_mismatch_description("[name] \"Tiberius Caesar\" was \"Julius Caesar\""));
    }
}
