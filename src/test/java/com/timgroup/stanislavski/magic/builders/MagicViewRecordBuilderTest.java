package com.timgroup.stanislavski.magic.builders;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.timgroup.stanislavski.interpreters.AddressesProperty;
import com.timgroup.stanislavski.magic.matchers.MagicJavaBeanMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MagicViewRecordBuilderTest {
    
    public static interface ViewRecord {
        BuilderFactory<Builder, ViewRecord> factory = BuilderFactory.validating(Builder.class).against(ViewRecord.class);
        
        String getName();
        int getAge();
        String favouriteColour();
        String quest();
    
        public static interface Builder extends Supplier<ViewRecord> {
            Builder withName(String name);
            Builder withAge(Integer age);
            Builder withFavouriteColour(String favouriteColour);
            
            Builder with_name(String name);
            Builder with_age(Integer age);
            Builder with_favourite_colour(String favouriteColour);

            @AddressesProperty("quest")
            Builder havingTheNobleQuest(String quest);
        }
    }

    public static interface RecordMatcher extends Matcher<ViewRecord> {
        RecordMatcher with_name(String name);
        RecordMatcher with_name(Matcher<? super String> name);
        RecordMatcher with_age(Integer age);
        RecordMatcher with_favourite_colour(String favouriteColour);
        @AddressesProperty("quest")
        RecordMatcher having_the_noble_quest(String quest);
    }

    private RecordMatcher a_record() {
        return MagicJavaBeanMatcher.matching(ViewRecord.class).using(RecordMatcher.class);
    }

    @Test
    public void builds_a_record_using_underscored_methods() {
        ViewRecord record = ViewRecord.factory.get()
                                              .with_name("Dominic")
                                              .with_age(37)
                                              .with_favourite_colour("Crimson")
                                              .get();

        assertThat(record, a_record().with_name(Matchers.startsWith("D")).with_age(37).with_favourite_colour("Crimson"));
    }

    @Test
    public void builds_a_record_using_camelCased_methods() {
        ViewRecord record = ViewRecord.factory.get()
                                              .withName("Dominic")
                                              .withAge(37)
                                              .withFavouriteColour("Crimson")
                                              .get();

        assertThat(record.getName(), is("Dominic"));
        assertThat(record.getAge(), is(37));
        assertThat(record.favouriteColour(), is("Crimson"));
    }

    @Test
    public void permits_aliasing_of_methods_using_annotations() {
        ViewRecord record = ViewRecord.factory.get()
                                              .havingTheNobleQuest("I seek the Castle Anthrax!")
                                              .get();

        assertThat(record, a_record().having_the_noble_quest("I seek the Castle Anthrax!"));
    }
}
