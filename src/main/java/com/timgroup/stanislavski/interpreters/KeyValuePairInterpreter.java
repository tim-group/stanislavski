package com.timgroup.stanislavski.interpreters;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.timgroup.stanislavski.recording.CallHistoryInterpreter;
import com.timgroup.stanislavski.reflection.MethodCall;

public class KeyValuePairInterpreter<K, V> implements CallHistoryInterpreter<Map<K, V>> {

    private final Function<MethodCall, ? extends K> keyInterpreter;
    private final Function<MethodCall, ? extends V> valueInterpreter;
    
    public KeyValuePairInterpreter(Function<MethodCall, ? extends K> keyInterpreter, Function<MethodCall, ? extends V> valueInterpreter) {
        this.keyInterpreter = keyInterpreter;
        this.valueInterpreter = valueInterpreter;
    }
    
    @Override
    public Map<K, V> apply(Iterable<MethodCall> callHistory) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (MethodCall methodCall : callHistory) {
            builder.put(keyInterpreter.apply(methodCall), valueInterpreter.apply(methodCall));
        }
        return builder.build();
    }

}
