package com.timgroup.stanislavski.magic.builders;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.timgroup.karg.keywords.typed.TypedKeywordArgument;
import com.timgroup.karg.keywords.typed.TypedKeywordArguments;
import com.timgroup.karg.reflection.ReflectiveAccessorFactory;

public abstract class MapToRecordConverter<T> implements Function<Map<String, Object>, T>{
    
    public static <T> MapToRecordConverter<T> forClass(Class<T> recordType) {
        for (Constructor<?> constructor : recordType.getConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                return new AssigningMapToRecordConverter<T>(recordType);
            }
            
            if (constructor.getParameterTypes().length == 1) {
                if (constructor.getParameterTypes()[0].equals(TypedKeywordArguments.class)) {
                    return new TypedKeywordArgumentMapToRecordConverter<T>(recordType);
                }
            }
        }
        return null;
    }
    
    private static final class AssigningMapToRecordConverter<T> extends MapToRecordConverter<T> {
        private final ReflectiveAccessorFactory<T> accessorFactory;
        private final Supplier<T> blankRecordSupplier;
    
        public AssigningMapToRecordConverter(Class<T> recordType) {
            accessorFactory = ReflectiveAccessorFactory.forClass(recordType);
            this.blankRecordSupplier = DefaultConstructorSupplier.of(recordType);
        }
        
        public T apply(Map<String, Object> fieldValues) {
            final T record = blankRecordSupplier.get();
            for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
                accessorFactory.getSetter(entry.getKey()).set(record, entry.getValue());
            }
            return record;
        }
    }
    
    private static final class TypedKeywordArgumentMapToRecordConverter<T> extends MapToRecordConverter<T> {
        
        private final ComplexConstructorSupplier<T> supplier;
        private final Function<Map.Entry<String, Object>, TypedKeywordArgument<T>> keywordArgumentLookup;
        
        public TypedKeywordArgumentMapToRecordConverter(Class<T> recordType) {
            supplier = ComplexConstructorSupplier.of(recordType);
            this.keywordArgumentLookup = new KeywordArgumentBuilder<T>(recordType);
        }
        
        @Override
        public T apply(Map<String, Object> map) {
            Iterable<TypedKeywordArgument<T>> keywordArgs = Iterables.transform(map.entrySet(), keywordArgumentLookup);
            TypedKeywordArguments<T> arguments = TypedKeywordArguments.of(Lists.newArrayList(keywordArgs));
            return supplier.withArg(arguments).get();
        }
        
    }
}