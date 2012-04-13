package com.timgroup.stanislavski.interpreters;

import com.google.common.base.Function;
import com.timgroup.stanislavski.reflection.MethodCall;

public final class Interpreters {

    private Interpreters() {
    }

    public static final class KeyValuePairInterpreterBuilder<K> {
        private final Function<MethodCall, K> keyInterpreter;

        private KeyValuePairInterpreterBuilder(Function<MethodCall, K> keyInterpreter) {
            this.keyInterpreter = keyInterpreter;
        }

        public KeyValuePairInterpreter<K, Object> usingFirstArgument() {
            return obtainingValueWith(ExtractorFor.theFirstArgument());
        }

        public <V> KeyValuePairInterpreter<K, V> obtainingValueWith(Function<MethodCall, V> valueInterpreter) {
            return new KeyValuePairInterpreter<K, V>(keyInterpreter, valueInterpreter);
        }
    }

    public static <K> KeyValuePairInterpreterBuilder<K> keyValuePairInterpreter(Function<MethodCall, K> keyExtractor) {
        return new KeyValuePairInterpreterBuilder<K>(keyExtractor);
    }

}
