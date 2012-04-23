package com.timgroup.stanislavski.interpreters;

import java.lang.reflect.Method;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.timgroup.stanislavski.reflection.MethodCall;

import static org.hamcrest.MatcherAssert.assertThat;

public class InterpretersTest {

    private Function<String, String> toUpperCase() {
        return new Function<String, String>() {
            @Override public String apply(String arg0) {
                return arg0.toUpperCase();
            }
        };
    }
    
    public static interface TestInterface {
        public TestInterface foo(String value);
        
        @AddressesProperty("quux")
        public TestInterface baz(String value);
    }
    
    @Test public void
    constructs_a_name_value_interpreter_using_supplied_key_and_value_interpreters() throws SecurityException, NoSuchMethodException {
        KeyValuePairInterpreter<String, String> interpreter = 
                Interpreters.keyValuePairInterpreter(ExtractorFor.theMethodName()
                                                                 .compose(toUpperCase())
                                                                 .chain(AddressesProperty.OVERRIDER))
                            .obtainingValueWith(ExtractorFor.theFirstArgument()
                                                            .compose(Functions.toStringFunction()));
        
        List<MethodCall> callHistory = Lists.newArrayList(
            MethodCall.create(testMethodNamed("foo"), "bar"),
            MethodCall.create(testMethodNamed("baz"), "xyzzy")
        );
        assertThat(interpreter.apply(callHistory), Matchers.hasEntry("FOO", "bar"));
        assertThat(interpreter.apply(callHistory), Matchers.hasEntry("quux", "xyzzy"));
    }

    private Method testMethodNamed(String methodName) throws NoSuchMethodException {
        return TestInterface.class.getMethod(methodName, new Class<?>[] { String.class });
    }
    
}
