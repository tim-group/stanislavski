package com.timgroup.stanislavski.magic.builders;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.timgroup.stanislavski.interpreters.AddressesProperty;
import com.timgroup.stanislavski.interpreters.AnnotationOverride;
import com.timgroup.stanislavski.interpreters.ExtractorFor;
import com.timgroup.stanislavski.interpreters.Interpreters;
import com.timgroup.stanislavski.magic.MethodNameToPropertyNameTranslator;
import com.timgroup.stanislavski.recording.CallHistoryInterpreter;
import com.timgroup.stanislavski.recording.InterceptingMethodCallRecorder;
import com.timgroup.stanislavski.recording.InterpretingFinalCallHandler;
import com.timgroup.stanislavski.reflection.MethodCall;
import com.timgroup.stanislavski.reflection.MethodNameMatcher;

public final class MagicRecordBuilder {
    
    public static class MagicRecordBuilderMaker<T> {
        
        private final Class<T> recordType;
        private final Constructor<T> constructor;
        
        private MagicRecordBuilderMaker(Class<T> recordType) {
            this.recordType = recordType;
            this.constructor = null;
        }
        
        private MagicRecordBuilderMaker(Class<T> recordType, Constructor<T> constructor) {
            this.recordType = recordType;
            this.constructor = constructor;
        }
        
        public <I> I using(Class<I> interfaceType) {
            CallHistoryInterpreter<Map<String, Object>> interpreter =
                Interpreters.keyValuePairInterpreter(ExtractorFor.theMethodName()
                                                                 .compose(new MethodNameToPropertyNameTranslator())
                                                                 .chain(AnnotationOverride.<AddressesProperty, String>obtainingValueOf(AddressesProperty.class)))
                            .obtainingValueWith(ExtractorFor.theFirstArgument().compose(realise));
            
            MapToRecordConverter<T> converter = MapToRecordConverter.forClass(recordType, constructor);
            Function<Iterable<MethodCall>, T> recordBuildingInterpreter = Functions.compose(converter, interpreter);
            
            return InterceptingMethodCallRecorder.proxying(interfaceType,
                                                           new MethodNameMatcher("get", Supplier.class),
                                                           new InterpretingFinalCallHandler<T>(recordBuildingInterpreter));
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> MagicRecordBuilderMaker<T> building(Class<T> recordType) {
        if (recordType.isInterface()) {
            return new MagicRecordBuilderMaker<T>(recordType);
        }
        Preconditions.checkArgument(recordType.getConstructors().length == 1,
                "%s has more than one constructor - please specify which one to use", recordType);
        return building(recordType, (Constructor<T>) recordType.getConstructors()[0]);
    }
    
    public static <T> MagicRecordBuilderMaker<T> building(Class<T> recordType, Constructor<T> constructor) {
        return new MagicRecordBuilderMaker<T>(recordType, constructor);
    }
    
    private MagicRecordBuilder() { }
    
    private static final Function<Object, Object> realise = new Function<Object, Object>() {
        @Override
        public Object apply(Object putativeValue) {
            if (putativeValue instanceof Supplier) {
                return ((Supplier<?>) putativeValue).get();
            }
            return putativeValue;
        }
    };
}