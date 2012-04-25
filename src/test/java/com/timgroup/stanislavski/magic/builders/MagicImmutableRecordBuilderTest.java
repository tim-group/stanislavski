package com.timgroup.stanislavski.magic.builders;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.timgroup.stanislavski.interpreters.AddressesProperty;
import com.timgroup.stanislavski.magic.matchers.MagicJavaBeanMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MagicImmutableRecordBuilderTest {
    
    public static class ImmutableRecord {

        public final String name;
        public final int age;
        public final String favouriteColour;
        public final String quest;

        public ImmutableRecord(@AssignedTo("name") String name,
                               @AssignedTo(value="age") int age,
                               @AssignedTo("favouriteColour") String favouriteColour,
                               @AssignedTo("quest") String quest) {
            this.name = name;
            this.age = age;
            this.favouriteColour = favouriteColour;
            this.quest = quest;
        }
        
        public static interface Builder extends Supplier<ImmutableRecord> {
            Builder withName(String name);
            Builder withAge(Integer age);
            Builder withFavouriteColour(String favouriteColour);
            
            Builder with_name(String name);
            Builder with_age(Integer age);
            Builder with_favourite_colour(String favouriteColour);

            @AddressesProperty("quest")
            Builder havingTheNobleQuest(String quest);
        }

        public static Builder builder() {
            return MagicRecordBuilder.building(ImmutableRecord.class).using(ImmutableRecord.Builder.class);
        }
    }

    public static interface RecordMatcher extends Matcher<ImmutableRecord> {
        RecordMatcher with_name(String name);
        RecordMatcher with_name(Matcher<? super String> name);
        RecordMatcher with_age(Integer age);
        RecordMatcher with_favourite_colour(String favouriteColour);
        @AddressesProperty("quest")
        RecordMatcher having_the_noble_quest(String quest);
    }

    private RecordMatcher a_record() {
        return MagicJavaBeanMatcher.matching(ImmutableRecord.class).using(RecordMatcher.class);
    }

    @Test
    public void builds_a_record_using_underscored_methods() {
        ImmutableRecord record = ImmutableRecord.builder().with_name("Dominic").with_age(37).with_favourite_colour("Crimson").get();

        assertThat(record, a_record().with_name(Matchers.startsWith("D")).with_age(37).with_favourite_colour("Crimson"));
    }

    @Test
    public void builds_a_record_using_camelCased_methods() {
        ImmutableRecord record = ImmutableRecord.builder().withName("Dominic").withAge(37).withFavouriteColour("Crimson").get();

        assertThat(record.name, is("Dominic"));
        assertThat(record.age, is(37));
        assertThat(record.favouriteColour, is("Crimson"));
    }

    @Test
    public void permits_aliasing_of_methods_using_annotations() {
        ImmutableRecord.Builder builder = ImmutableRecord.builder();
        assertThat(builder.with_age(37).havingTheNobleQuest("I seek the Castle Anthrax!").get(), a_record().having_the_noble_quest("I seek the Castle Anthrax!"));
    }
}
