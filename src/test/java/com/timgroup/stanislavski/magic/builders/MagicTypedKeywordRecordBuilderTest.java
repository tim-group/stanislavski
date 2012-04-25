package com.timgroup.stanislavski.magic.builders;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.timgroup.karg.keywords.typed.TypedKeyword;
import com.timgroup.karg.keywords.typed.TypedKeywordArguments;
import com.timgroup.karg.keywords.typed.TypedKeywords;
import com.timgroup.stanislavski.interpreters.AddressesProperty;
import com.timgroup.stanislavski.magic.matchers.MagicJavaBeanMatcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MagicTypedKeywordRecordBuilderTest {

    public static class TypedKeywordRecord {
        
        public static final TypedKeyword<TypedKeywordRecord, String> NAME = TypedKeywords.newTypedKeyword();
        public final String name;
        
        public static final TypedKeyword<TypedKeywordRecord, Integer> AGE = TypedKeywords.newTypedKeyword();
        public final Integer age;
        
        public static final TypedKeyword<TypedKeywordRecord, String> FAVOURITE_COLOUR = TypedKeywords.newTypedKeyword();
        public final String favouriteColour;
        
        public static final TypedKeyword<TypedKeywordRecord, String> QUEST = TypedKeywords.newTypedKeyword();
        public final String quest;
        
        private final TypedKeywordArguments<TypedKeywordRecord> properties;
        
        public TypedKeywordRecord(TypedKeywordArguments<TypedKeywordRecord> args) {
            this.name = NAME.from(args);
            this.age = AGE.from(args);
            this.favouriteColour = FAVOURITE_COLOUR.from(args);
            this.quest = QUEST.from(args);
            this.properties = args;
        }
        
        public <T> T get(TypedKeyword<TypedKeywordRecord, T> property) {
            return property.from(properties);
        }
        
        public static interface Builder extends Supplier<TypedKeywordRecord> {
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
            return MagicRecordBuilder.building(TypedKeywordRecord.class).using(Builder.class);
        }
    }
    
    public static interface RecordMatcher extends Matcher<TypedKeywordRecord> {
        RecordMatcher with_name(String name);
        RecordMatcher with_name(Matcher<? super String> name);
        RecordMatcher with_age(Integer age);
        RecordMatcher with_favourite_colour(String favouriteColour);
        
        @AddressesProperty("quest")
        RecordMatcher having_the_noble_quest(String quest);
    }
    
    private RecordMatcher a_record() {
        return MagicJavaBeanMatcher.matching(TypedKeywordRecord.class).using(RecordMatcher.class);
    }
    
    @Test public void
    builds_a_record_using_underscored_methods() {
        TypedKeywordRecord record = TypedKeywordRecord.builder()
                .with_name("Dominic")
                .with_age(37)
                .with_favourite_colour("Crimson")
                .get();
        
        assertThat(record, a_record().with_name(Matchers.startsWith("D"))
                                                .with_age(37)
                                                .with_favourite_colour("Crimson"));
    }
    
    @Test public void
    builds_a_record_using_camelCased_methods() {
        TypedKeywordRecord record = TypedKeywordRecord.builder()
                .withName("Dominic")
                .withAge(37)
                .withFavouriteColour("Crimson")
                .get();
        
        assertThat(record.name, is("Dominic"));
        assertThat(record.age, is(37));
        assertThat(record.favouriteColour, is("Crimson"));
    }
    
    @Test public void
    permits_aliasing_of_methods_using_annotations() {
        TypedKeywordRecord record = TypedKeywordRecord.builder()
                .havingTheNobleQuest("I seek the Castle Anthrax!")
                .get();
        
        assertThat(record, a_record().having_the_noble_quest("I seek the Castle Anthrax!"));
    }
}
