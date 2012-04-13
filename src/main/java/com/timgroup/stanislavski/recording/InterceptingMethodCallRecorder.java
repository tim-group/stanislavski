package com.timgroup.stanislavski.recording;

import com.google.common.base.Predicate;
import com.timgroup.stanislavski.proxying.MethodCallHandler;
import com.timgroup.stanislavski.proxying.ProxyFactory;
import com.timgroup.stanislavski.reflection.MethodCall;

public class InterceptingMethodCallRecorder<T> implements MethodCallHandler {
    
    private final Predicate<MethodCall> finalCallMatcher;
    private final FinalCallHandler<T> finalCallHandler;
    private final ProxyFactory proxyFactory;
    private final MethodCallRecorder recorder;
    
    public static <I, T, R> I proxying(Class<I> interfaceType,
                                       Predicate<MethodCall> finalCallMatcher,
                                       FinalCallHandler<T> finalCallHandler) {
        return new InterceptingMethodCallRecorder<T>(finalCallMatcher, finalCallHandler).proxyFactory.getProxy(interfaceType);
    }
    
    private InterceptingMethodCallRecorder(Predicate<MethodCall> predicate,
                                           FinalCallHandler<T> methodCallDispatcher) {
        this.finalCallMatcher = predicate;
        this.finalCallHandler = methodCallDispatcher;
        this.proxyFactory = ProxyFactory.forMethodCallHandler(this);
        this.recorder = new MethodCallRecorder();
    }
    
    @Override public Object handle(MethodCall methodCall) {
        if (finalCallMatcher.apply(methodCall)) {
            return finalCallHandler.handle(methodCall, recorder.callHistory());
        }
        recorder.record(methodCall);
        return proxyFactory.getProxy(methodCall.returnType());
    }
}
