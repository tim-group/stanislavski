package com.timgroup.stanislavski.recording;


import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.timgroup.stanislavski.matchers.AMethodCall;
import com.timgroup.stanislavski.matchers.AnArgument;
import com.timgroup.stanislavski.recording.CallHistoryInterpreter;
import com.timgroup.stanislavski.recording.CallRecordingSupplier;
import com.timgroup.stanislavski.reflection.MethodCall;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class CallRecordingSupplierTest {

    public static final class Point { }
    
    private final Mockery context = new Mockery();
    
    @SuppressWarnings("unchecked")
    private final CallHistoryInterpreter<Point> translator = context.mock(CallHistoryInterpreter.class);
    
    public static interface PointBuilder extends Supplier<Point> {
        public PointBuilder with_x(int x);
        public PointBuilder with_y(int y);
    }
    
    @SuppressWarnings("unchecked")
    @Test public void
    uses_supplied_interpreter_to_translate_call_history_into_object() {
        PointBuilder builder = CallRecordingSupplier.proxying(PointBuilder.class, translator);
        
        final Point testPoint = new Point();
        
        context.checking(new Expectations() {{
            oneOf(translator).apply(with(Matchers.<MethodCall>contains(
                AMethodCall.to("with_x").of(PointBuilder.class).with(AnArgument.of(23)),
                AMethodCall.to("with_y").of(PointBuilder.class).with(AnArgument.of(17)))
            )); will(returnValue(testPoint));
        }});
        
        assertThat(builder.with_x(23).with_y(17).get(), equalTo(testPoint));
        context.assertIsSatisfied();
    }
}
