package com.timgroup.stanislavski.magic.builders;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.timgroup.karg.keywords.typed.TypedKeywordArguments;
import com.timgroup.karg.valuetypes.ValueType;
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
        
        protected final Class<T> recordType;
        protected final Constructor<T> constructor;
        
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
            
            MapToRecordConverter<T> converter = getMapToRecordConverter();
            Function<Iterable<MethodCall>, T> recordBuildingInterpreter = Functions.compose(converter, interpreter);
            
            return InterceptingMethodCallRecorder.proxying(interfaceType,
                                                           new MethodNameMatcher("get", Supplier.class),
                                                           new InterpretingFinalCallHandler<T>(recordBuildingInterpreter));
        }
        
        protected MapToRecordConverter<T> getMapToRecordConverter() {
            return MapToRecordConverter.forClass(recordType, constructor);
        }
    }
    
    public static class MagicValueTypeBuilderMaker<T extends ValueType<T>> extends MagicRecordBuilderMaker<T> {
        
        private final TypedKeywordArguments<T> fields;
        public MagicValueTypeBuilderMaker(Class<T> recordType, Constructor<T> constructor, TypedKeywordArguments<T> fields) {
            super(recordType, constructor);
            this.fields = fields;
        }
    
        protected MapToRecordConverter<T> getMapToRecordConverter() {
            return MapToRecordConverter.forClass(recordType, constructor, fields);
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
    
    @SuppressWarnings("unchecked")
    public static <T extends ValueType<T>> MagicRecordBuilderMaker<T> updating(T instance) {
        Class<T> recordType = (Class<T>) instance.getClass();
        Constructor<T> constructor;
        try {
            constructor = recordType.getDeclaredConstructor(TypedKeywordArguments.class);
        } catch (SecurityException e) {
            throw new RuntimeException(String.format("Cannot update class %s reflectively", recordType));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Class %s does not have a TypedKeywordArguments constructor"));
        }
        
        return new MagicValueTypeBuilderMaker<T>(recordType, constructor, instance.fields());
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