package com.timgroup.stanislavski.magic.builders;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class ViewRecordTest {

    public interface View {
        String foo();
        int getBar();
        boolean isBaz();
    }
    
    private final Map<String, Object> properties1 = ImmutableMap.<String, Object>builder()
            .put("foo", "foo")
            .put("bar", 42)
            .put("baz", true)
            .build();
    
    private final Map<String, Object> properties2 = ImmutableMap.<String, Object>builder()
            .put("foo", "foo")
            .put("bar", 43)
            .put("baz", true)
            .build();
    
    @Test public void
    proxies_to_underlying_property_map() {
        View view = ViewRecord.proxying(View.class, properties1);
        
        assertThat(view.foo(), is("foo"));
        assertThat(view.getBar(), is(42));
        assertThat(view.isBaz(), is(true));
    }
    
    @Test public void
    views_are_equal_when_their_properties_are_equal() {
        View view1 = ViewRecord.proxying(View.class, properties1);
        View view2 = ViewRecord.proxying(View.class, properties1);
        
        assertThat(view1, is(equalTo(view2)));
    }
    
    @Test public void
    views_have_equal_hashcodes_when_their_properties_are_equal() {
        View view1 = ViewRecord.proxying(View.class, properties1);
        View view2 = ViewRecord.proxying(View.class, properties1);
        
        assertThat(view1.hashCode(), is(view2.hashCode()));
    }
    
    @Test public void
    views_are_unequal_when_their_properties_are_unequal() {
        View view1 = ViewRecord.proxying(View.class, properties1);
        View view2 = ViewRecord.proxying(View.class, properties2);
        
        assertThat(view1, is(not(equalTo(view2))));
    }
    
    @Test public void
    views_have_readable_string_representations() {
        View view = ViewRecord.proxying(View.class, properties1);
        
        assertThat(view.toString(), Matchers.allOf(Matchers.containsString("foo: foo"),
                                                  Matchers.containsString("getBar: 42"),
                                                  Matchers.containsString("isBaz: true")));
    }

}
