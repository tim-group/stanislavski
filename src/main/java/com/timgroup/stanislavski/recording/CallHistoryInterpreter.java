package com.timgroup.stanislavski.recording;

import com.google.common.base.Function;
import com.timgroup.stanislavski.reflection.MethodCall;

public interface CallHistoryInterpreter<T> extends Function<Iterable<MethodCall>, T> { }