package com.timgroup.stanislavski.recording;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.timgroup.stanislavski.reflection.MethodCall;
import com.timgroup.stanislavski.reflection.MethodNameMatcher;

public final class CallRecordingSupplier {

    private CallRecordingSupplier() { }

    public static <I, R> I proxying(final Class<I> interfaceType,
                                    Function<Iterable<MethodCall>, R> interpreter) {
        return InterceptingMethodCallRecorder.proxying(interfaceType,
                                                       new MethodNameMatcher("get", Supplier.class),
                                                       new InterpretingFinalCallHandler<R>(interpreter));
    }

}