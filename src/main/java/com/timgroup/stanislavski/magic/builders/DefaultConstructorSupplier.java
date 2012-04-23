package com.timgroup.stanislavski.magic.builders;

import com.google.common.base.Supplier;

public final class DefaultConstructorSupplier<T> implements Supplier<T> {
    private final Class<T> type;
    
    public static <T> DefaultConstructorSupplier<T> of(Class<T> type) {
        return new DefaultConstructorSupplier<T>(type);
    }
    
    private DefaultConstructorSupplier(Class<T> type) {
        this.type = type;
    }
    
    @Override public T get() {
        try {
            return type.newInstance();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}