package com.timgroup.stanislavski.proxying;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.timgroup.stanislavski.reflection.MethodCall;

public class InvocationHandlerAdapter implements InvocationHandler {

    private final MethodCallHandler handler;
    
    public InvocationHandlerAdapter(MethodCallHandler handler) {
        this.handler = handler;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("equals".equals(method.getName())) {
            return proxy == args[0];
        }
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        MethodCall methodCall = MethodCall.create(method, args);
        return handler.handle(methodCall);
    }

}
