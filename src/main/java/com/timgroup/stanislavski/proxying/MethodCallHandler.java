package com.timgroup.stanislavski.proxying;

import com.timgroup.stanislavski.reflection.MethodCall;

public interface MethodCallHandler {
    Object handle(MethodCall methodCall);
}