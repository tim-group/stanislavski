package com.timgroup.stanislavski.magic.builders;

import java.lang.reflect.Method;

import com.google.common.base.Supplier;
import com.timgroup.karg.reflection.ReflectiveAccessorFactory;
import com.timgroup.stanislavski.magic.MethodNameToPropertyNameTranslator;
import com.timgroup.stanislavski.reflection.MethodCall;

public class BuilderFactory<I extends Supplier<T>, T> implements Supplier<I> {

    private final Class<T> recordType;
    private final Class<I> interfaceClass;

    public static <I extends Supplier<T>, T> BuilderFactoryMaker<I, T> validating(Class<I> interfaceClass) {
        return new BuilderFactoryMaker<I, T>(interfaceClass);
    }
    
    public static class BuilderFactoryMaker<I extends Supplier<T>, T> {
        private final Class<I> interfaceClass;
        
        private BuilderFactoryMaker(Class<I> interfaceClass) {
            this.interfaceClass = interfaceClass;
        }
        
        public BuilderFactory<I, T> against(Class<T> recordType) {
            ReflectiveAccessorFactory<T> factory = ReflectiveAccessorFactory.forClass(recordType);
            for (Method method : interfaceClass.getDeclaredMethods()) {
                MethodCall methodCall = MethodCall.create(method);
                String propertyName = MethodNameToPropertyNameTranslator.interpret(methodCall);
                factory.getGetter(propertyName);
            }
            return new BuilderFactory<I, T>(recordType, interfaceClass);
        }
    }
    
    private BuilderFactory(Class<T> recordType, Class<I> interfaceClass) {
        this.recordType = recordType;
        this.interfaceClass = interfaceClass;
    }
    
    @Override public I get() {
        return MagicRecordBuilder.building(recordType).using(interfaceClass);
    }
}
