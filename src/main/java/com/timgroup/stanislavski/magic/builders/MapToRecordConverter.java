package com.timgroup.stanislavski.magic.builders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.timgroup.karg.keywords.typed.TypedKeywordArgument;
import com.timgroup.karg.keywords.typed.TypedKeywordArguments;
import com.timgroup.karg.reflection.ReflectiveAccessorFactory;

public abstract class MapToRecordConverter<T> implements Function<Map<String, Object>, T>{
    
    @SuppressWarnings("unchecked")
    public static <T> MapToRecordConverter<T> forClass(Class<T> recordType, Constructor<T> constructor) {
        if (constructor == null) {
            return new ProxyGeneratingMapToRecordConverter<T>(recordType);
        }
        int numberOfParameters = constructor.getParameterTypes().length;
        if (numberOfParameters == 0) {
            return new AssigningMapToRecordConverter<T>(recordType);
        }
        
        if (numberOfParameters == 1 && constructor.getParameterTypes()[0].equals(TypedKeywordArguments.class)) {
            return new TypedKeywordArgumentMapToRecordConverter<T>(recordType, TypedKeywordArguments.<T>of());
        }
        
        return new AnnotatedConstructorMapToRecordConverter<T>(recordType, constructor);
    }
        
    public static <T> MapToRecordConverter<T> forClass(Class<T> recordType, Constructor<T> constructor, TypedKeywordArguments<T> fields) {
        int numberOfParameters = constructor.getParameterTypes().length;
        if (numberOfParameters == 1 && constructor.getParameterTypes()[0].equals(TypedKeywordArguments.class)) {
            return new TypedKeywordArgumentMapToRecordConverter<T>(recordType, fields);
        }
        
        throw new IllegalArgumentException("Cannot update a type that has no TypedKeywordArguments constructor");
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
    
    private static final class ProxyGeneratingMapToRecordConverter<T> extends MapToRecordConverter<T> {
        private final Class<T> recordType;
        
        public ProxyGeneratingMapToRecordConverter(Class<T> recordType) {
            this.recordType = recordType;
        }

        @Override public T apply(Map<String, Object> properties) {
            return ViewRecord.proxying(recordType, properties);
        }
    }
    
    private static final class AnnotatedConstructorMapToRecordConverter<T> extends MapToRecordConverter<T> {
        private final Constructor<T> constructor;
        private final Class<T> recordType;
        
        public AnnotatedConstructorMapToRecordConverter(Class<T> recordType, Constructor<T> constructor) {
            this.recordType = recordType;
            this.constructor = constructor;
        }

        @Override
        public T apply(Map<String, Object> map) {
            Object[] args = populateArgs(map);
            try {
                return constructor.newInstance(args);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        private Object[] populateArgs(Map<String, Object> map) {
            Object[] args = new Object[constructor.getParameterTypes().length];
            int i = 0;
            for (Annotation[] annotations : constructor.getParameterAnnotations()) {
                AssignedTo assignedTo = (AssignedTo) Iterables.find(Lists.newArrayList(annotations), Predicates.instanceOf(AssignedTo.class), null);
                Preconditions.checkNotNull(assignedTo, "Constructor parameter #%d for %s has no @AssignedTo annotation", i, recordType);
                args[i] = map.get(assignedTo.value());
                i += 1;
            }
            return args;
        }
    }
    
    private static final class TypedKeywordArgumentMapToRecordConverter<T> extends MapToRecordConverter<T> {
        
        private final ComplexConstructorSupplier<T> supplier;
        private final Function<Map.Entry<String, Object>, TypedKeywordArgument<T>> keywordArgumentLookup;
        private TypedKeywordArguments<T> fields;
        
        public TypedKeywordArgumentMapToRecordConverter(Class<T> recordType, TypedKeywordArguments<T> fields) {
            this.fields = fields;
            supplier = ComplexConstructorSupplier.of(recordType);
            this.keywordArgumentLookup = new KeywordArgumentBuilder<T>(recordType);
        }
        
        @Override
        public T apply(Map<String, Object> map) {
            Iterable<TypedKeywordArgument<T>> keywordArgs = Iterables.transform(map.entrySet(), keywordArgumentLookup);
            TypedKeywordArguments<T> arguments = fields.with(Lists.newArrayList(keywordArgs));
            return supplier.withArg(arguments).get();
        }
    }
}