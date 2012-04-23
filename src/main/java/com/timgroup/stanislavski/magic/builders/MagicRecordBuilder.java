package com.timgroup.stanislavski.magic.builders;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.timgroup.stanislavski.interpreters.AddressesProperty;
import com.timgroup.stanislavski.interpreters.ExtractorFor;
import com.timgroup.stanislavski.interpreters.Interpreters;
import com.timgroup.stanislavski.magic.MethodNameToPropertyNameTranslator;
import com.timgroup.stanislavski.recording.CallHistoryInterpreter;
import com.timgroup.stanislavski.recording.InterceptingMethodCallRecorder;
import com.timgroup.stanislavski.recording.InterpretingFinalCallHandler;
import com.timgroup.stanislavski.reflection.MethodCall;
import com.timgroup.stanislavski.reflection.MethodNameMatcher;

public final class MagicRecordBuilder {
    
    private MagicRecordBuilder() { }
    
    public static <I, T> I proxying(Class<I> interfaceType, Class<T> recordType) {
        CallHistoryInterpreter<Map<String, Object>> interpreter =
            Interpreters.keyValuePairInterpreter(ExtractorFor.theMethodName()
                                                             .compose(new MethodNameToPropertyNameTranslator())
                                                             .chain(AddressesProperty.OVERRIDER))
                        .usingFirstArgument();
        
        MapToRecordConverter<T> converter = MapToRecordConverter.forClass(recordType);
        Function<Iterable<MethodCall>, T> recordBuildingInterpreter = Functions.compose(converter, interpreter);
        
        return InterceptingMethodCallRecorder.proxying(interfaceType,
                                                       new MethodNameMatcher("get", Supplier.class),
                                                       new InterpretingFinalCallHandler<T>(recordBuildingInterpreter));
    }
}