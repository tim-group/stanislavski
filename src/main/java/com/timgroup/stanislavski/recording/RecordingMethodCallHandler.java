package com.timgroup.stanislavski.recording;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.timgroup.stanislavski.proxying.InvocationHandlerAdapter;
import com.timgroup.stanislavski.proxying.MethodCallHandler;
import com.timgroup.stanislavski.reflection.MethodCall;

public final class RecordingMethodCallHandler implements MethodCallHandler {
    
    private final MethodCallRecorder recorder = new MethodCallRecorder();
    private final InvocationHandler invocationHandler= new InvocationHandlerAdapter(this);
    
    @SuppressWarnings("unchecked")
    public <I> I getProxy(Class<I> interfaceType) {
        return (I) Proxy.newProxyInstance(interfaceType.getClassLoader(),
                                          new Class<?>[] { interfaceType },
                                          invocationHandler);
    }

    public Iterable<MethodCall> callHistory() {
        return recorder.callHistory();
    }

    @Override
    public Object handle(MethodCall methodCall) {
        recorder.record(methodCall);
        return getProxy(methodCall.returnType());
    }
}