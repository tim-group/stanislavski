package com.timgroup.stanislavski.recording;


import org.hamcrest.Matchers;
import org.junit.Test;

import com.timgroup.stanislavski.matchers.AMethodCall;
import com.timgroup.stanislavski.matchers.AnArgument;
import com.timgroup.stanislavski.recording.RecordingMethodCallHandler;
import com.timgroup.stanislavski.reflection.MethodCall;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class RecordingMethodCallHandlerTest {
    
    public static interface TestInterface1 {
        TestInterface1 method1(String arg1);
        TestInterface2 method2(Integer arg2);
    }
    
    public static interface TestInterface2 {
        TestInterface1 method3(String arg1);
    }
    
    @SuppressWarnings("unchecked")
    @Test public void
    records_calls_made_to_methods_declared_in_proxied_interface() {
        RecordingMethodCallHandler recorder = new RecordingMethodCallHandler();
        TestInterface1 proxy = recorder.getProxy(TestInterface1.class);
        
        proxy.method1("Hello World").method2(23);
        
        assertThat(recorder.callHistory(), Matchers.<MethodCall>contains(
            AMethodCall.to("method1").of(TestInterface1.class).with(AnArgument.of("Hello World")),
            AMethodCall.to("method2").of(TestInterface1.class).with(AnArgument.of(23))
         ));
    }
    
    @Test public void
    a_proxy_is_equal_to_itself() {
        RecordingMethodCallHandler recorder = new RecordingMethodCallHandler();
        TestInterface1 proxy = recorder.getProxy(TestInterface1.class);
        
        assertThat(proxy, equalTo(proxy));
    }
    
    @SuppressWarnings("unchecked")
    @Test public void
    methods_chain_across_interfaces() {
        RecordingMethodCallHandler recorder = new RecordingMethodCallHandler();
        TestInterface1 proxy = recorder.getProxy(TestInterface1.class);
        
        proxy.method2(23).method3("Goodbye cruel world");
        assertThat(recorder.callHistory(),  Matchers.<MethodCall>contains(
            AMethodCall.to("method2").of(TestInterface1.class).with(AnArgument.of(23)),
            AMethodCall.to("method3").of(TestInterface2.class).with(AnArgument.of("Goodbye cruel world"))
        ));
    }
    
}
