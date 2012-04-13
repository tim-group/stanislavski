package com.timgroup.stanislavski.recording;

import com.timgroup.stanislavski.reflection.MethodCall;

public interface FinalCallHandler<T> {
    T handle(MethodCall closingCall, Iterable<MethodCall> callHistory);
}