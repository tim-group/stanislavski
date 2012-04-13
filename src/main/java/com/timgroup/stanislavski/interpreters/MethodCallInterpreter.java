package com.timgroup.stanislavski.interpreters;

import com.google.common.base.Function;
import com.timgroup.stanislavski.reflection.MethodCall;

public interface MethodCallInterpreter<T> extends Function<MethodCall, T> { }
