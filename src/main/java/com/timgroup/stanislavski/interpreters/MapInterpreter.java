package com.timgroup.stanislavski.interpreters;

import java.util.Map;

import com.google.common.base.Function;

public interface MapInterpreter<K, V, T> extends Function<Map<K, V>, T> { }
