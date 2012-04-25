package com.timgroup.stanislavski.magic.builders;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ComplexConstructorSupplier<T> implements Supplier<T> {
    
    private final Class<T> type;
    private final ImmutableList.Builder<Supplier<Object>> args = ImmutableList.builder();
    
    public static <T> ComplexConstructorSupplier<T> of(Class<T> type) {
        return new ComplexConstructorSupplier<T>(type);
    }
    
    private ComplexConstructorSupplier(Class<T> type) {
        this.type = type;
    }
    
    public ComplexConstructorSupplier<T> withArg(Object o) {
        args.add(Suppliers.ofInstance(o));
        return this;
    }
    
    public ComplexConstructorSupplier<T> withArgs(Object...args) {
        for (Object arg : args) {
            withArg(arg);
        }
        return this;
    }
    
    @Override
    public T get() {
        List<Supplier<Object>> suppliers = args.build();
        Object[] supplied = Lists.transform(suppliers, Suppliers.supplierFunction()).toArray(new Object[suppliers.size()]);
        Class<?>[] types = new Class<?>[suppliers.size()];
        for (int i=0; i<types.length; i++) {
            types[i] = supplied[i].getClass();
        }
        try {
            return type.getDeclaredConstructor(types).newInstance(supplied);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

}
