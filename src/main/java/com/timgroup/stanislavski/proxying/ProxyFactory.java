package com.timgroup.stanislavski.proxying;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public final class ProxyFactory {
    
    private final InvocationHandler invocationHandler;

    public static ProxyFactory forMethodCallHandler(MethodCallHandler handler) {
        return new ProxyFactory(new InvocationHandlerAdapter(handler));
    }
    
    public ProxyFactory(InvocationHandler invocationHandler) {
        this.invocationHandler = invocationHandler;
    }
    
    @SuppressWarnings("unchecked")
    public <I> I getProxy(Class<I> interfaceType) {
        return (I) Proxy.newProxyInstance(interfaceType.getClassLoader(),
                                           new Class<?>[] { interfaceType },
                                           invocationHandler);
    }
}