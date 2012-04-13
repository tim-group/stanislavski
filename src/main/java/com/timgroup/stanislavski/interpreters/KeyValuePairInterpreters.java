package com.timgroup.stanislavski.interpreters;

import com.google.common.base.Function;
import com.timgroup.stanislavski.reflection.MethodCall;

public final class KeyValuePairInterpreters {

    private KeyValuePairInterpreters() {
    }

    public static final class ExtractorSpecifier<K> {
        private final Function<MethodCall, K> keyInterpreter;

        private ExtractorSpecifier(Function<MethodCall, K> keyInterpreter) {
            this.keyInterpreter = keyInterpreter;
        }

        public KeyValuePairInterpreter<K, Object> usingFirstArgument() {
            return obtainingValueWith(ExtractorFor.theFirstArgument());
        }

        public <V> KeyValuePairInterpreter<K, V> obtainingValueWith(Function<MethodCall, V> valueInterpreter) {
            return new KeyValuePairInterpreter<K, V>(keyInterpreter, valueInterpreter);
        }
    }

    public static ExtractorSpecifier<String> nameValuePairInterpreter(Function<MethodCall, String> nameExtractor) {
        return new ExtractorSpecifier<String>(nameExtractor);
    }

}
