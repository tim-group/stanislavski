package com.timgroup.stanislavski.recording;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.timgroup.stanislavski.matchers.AMethodCall;
import com.timgroup.stanislavski.matchers.AnArgument;
import com.timgroup.stanislavski.reflection.MethodCall;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class InterceptingMethodCallRecorderTest {

    private final Mockery context = new Mockery();
    
    @SuppressWarnings("unchecked")
    private final Predicate<MethodCall> finalCallMatcher = context.mock(Predicate.class);
    
    @SuppressWarnings("unchecked")
    private final FinalCallHandler<String> finalCallHandler = context.mock(FinalCallHandler.class);
    
    public static interface Greeter {
        Greeter withName(String name);
        Greeter withGreeting(String greeting);
        String greet();
    }
    
    @Test
    public void passes_call_history_to_final_call_handler_if_call_matches_predicate() {
        Greeter greeter = InterceptingMethodCallRecorder.proxying(Greeter.class,
                                                                  finalCallMatcher,
                                                                  finalCallHandler);
        
        context.checking(new Expectations() {{
            oneOf(finalCallMatcher).apply(with(AMethodCall.to("withName").of(Greeter.class).with(AnArgument.of("Dominic"))));
               will(returnValue(false));
            oneOf(finalCallMatcher).apply(with(AMethodCall.to("withGreeting").of(Greeter.class).with(AnArgument.of("Hello"))));
               will(returnValue(false));
            oneOf(finalCallMatcher).apply(with(AMethodCall.to("greet").of(Greeter.class)));
               will(returnValue(true));
            
            oneOf(finalCallHandler).handle(with(AMethodCall.to("greet").of(Greeter.class)),
                                           with(containsCalls(AMethodCall.to("withName").of(Greeter.class).with(AnArgument.of("Dominic")),
                                                              AMethodCall.to("withGreeting").of(Greeter.class).with(AnArgument.of("Hello"))))); 
               will(returnValue("Hello Dominic"));
        }});
        
        assertThat(greeter.withName("Dominic")
                          .withGreeting("Hello")
                          .greet(),
                   equalTo("Hello Dominic"));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Matcher<Iterable<MethodCall>> containsCalls(AMethodCall<?>... methodCalls) {
        return (Matcher)Matchers.contains(methodCalls);
    };

}
