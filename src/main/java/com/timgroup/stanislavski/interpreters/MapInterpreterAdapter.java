package com.timgroup.stanislavski.interpreters;

import java.util.Map;

import com.google.common.base.Function;
import com.timgroup.stanislavski.recording.CallHistoryInterpreter;
import com.timgroup.stanislavski.reflection.MethodCall;

public class MapInterpreterAdapter<K, V, T> implements CallHistoryInterpreter<T> {

    private final Function<Iterable<MethodCall>, Map<K, V>> keyValuePairInterpreter;
    private final Function<Map<K, V>, T> mapInterpreter;
    
    public MapInterpreterAdapter(Function<Iterable<MethodCall>, Map<K, V>> keyValuePairInterpreter,
                                 Function<Map<K, V>, T> mapInterpreter) {
                                    this.keyValuePairInterpreter = keyValuePairInterpreter;
                                    this.mapInterpreter = mapInterpreter;
    }
    
    @Override
    public T apply(Iterable<MethodCall> arg0) {
        Map<K, V> map = keyValuePairInterpreter.apply(arg0);
        return mapInterpreter.apply(map);
    }

}
