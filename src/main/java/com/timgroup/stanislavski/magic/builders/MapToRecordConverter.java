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
    public static <T> MapToRecordConverter<T> forClass(Class<T> recordType) {
        Preconditions.checkArgument(recordType.getConstructors().length == 1,
                "%s has more than one constructor - please specify which one to use", recordType);
        return forClass(recordType, (Constructor<T>) recordType.getConstructors()[0]);
    }
    
    public static <T> MapToRecordConverter<T> forClass(Class<T> recordType, Constructor<T> constructor) {
        int numberOfParameters = constructor.getParameterTypes().length;
        if (numberOfParameters == 0) {
            return new AssigningMapToRecordConverter<T>(recordType);
        }
        
        if (numberOfParameters == 1 && constructor.getParameterTypes()[0].equals(TypedKeywordArguments.class)) {
            return new TypedKeywordArgumentMapToRecordConverter<T>(recordType);
        }
        
        return new AnnotatedConstructorMapToRecordConverter<T>(recordType, constructor);
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