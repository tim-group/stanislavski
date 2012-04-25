package com.timgroup.stanislavski.magic.builders;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.timgroup.stanislavski.interpreters.AddressesProperty;
import com.timgroup.stanislavski.magic.matchers.MagicJavaBeanMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MagicBeanRecordBuilderTest {
    
    public static class BeanRecord {

        private String name;
        private int age = 0;
        private String favouriteColour;
        private String quest;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String getFavouriteColour() {
            return favouriteColour;
        }

        public void setFavouriteColour(String favouriteColour) {
            this.favouriteColour = favouriteColour;
        }

        public String getQuest() {
            return quest;
        }

        public void setQuest(String quest) {
            this.quest = quest;
        }

        public static interface Builder extends Supplier<BeanRecord> {
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
            return MagicRecordBuilder.proxying(BeanRecord.Builder.class, BeanRecord.class);
        }
    }

    public static interface RecordMatcher extends Matcher<BeanRecord> {
        RecordMatcher with_name(String name);
        RecordMatcher with_name(Matcher<? super String> name);
        RecordMatcher with_age(Integer age);
        RecordMatcher with_favourite_colour(String favouriteColour);
        @AddressesProperty("quest")
        RecordMatcher having_the_noble_quest(String quest);
    }

    private RecordMatcher a_record() {
        return MagicJavaBeanMatcher.matching(BeanRecord.class).using(RecordMatcher.class);
    }

    @Test
    public void builds_a_record_using_underscored_methods() {
        BeanRecord.Builder builder = MagicRecordBuilder.proxying(BeanRecord.Builder.class, BeanRecord.class);
        BeanRecord record = builder.with_name("Dominic").with_age(37).with_favourite_colour("Crimson").get();

        assertThat(record, a_record().with_name(Matchers.startsWith("D")).with_age(37).with_favourite_colour("Crimson"));
    }

    @Test
    public void builds_a_record_using_camelCased_methods() {
        BeanRecord.Builder builder = MagicRecordBuilder.proxying(BeanRecord.Builder.class, BeanRecord.class);
        BeanRecord record = builder.withName("Dominic").withAge(37).withFavouriteColour("Crimson").get();

        assertThat(record.name, is("Dominic"));
        assertThat(record.age, is(37));
        assertThat(record.favouriteColour, is("Crimson"));
    }

    @Test
    public void permits_aliasing_of_methods_using_annotations() {
        BeanRecord.Builder builder = MagicRecordBuilder.proxying(BeanRecord.Builder.class, BeanRecord.class);
        BeanRecord record = builder.havingTheNobleQuest("I seek the Castle Anthrax!").get();

        assertThat(record, a_record().having_the_noble_quest("I seek the Castle Anthrax!"));
    }
}
