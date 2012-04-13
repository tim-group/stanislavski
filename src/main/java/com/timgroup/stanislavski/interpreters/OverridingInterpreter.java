package com.timgroup.stanislavski.interpreters;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Suppliers;
import com.timgroup.stanislavski.reflection.MethodCall;

public class OverridingInterpreter<T> implements MethodCallInterpreter<T> {

    private final Function<MethodCall, T> defaultInterpreter;
    private final Function<MethodCall, Optional<T>> overrider;
    
    public OverridingInterpreter(Function<MethodCall, T> defaultInterpreter,
                                 Function<MethodCall, Optional<T>> overrider) {
        this.defaultInterpreter = defaultInterpreter;
        this.overrider = overrider;
    }

    @Override
    public T apply(MethodCall methodCall) {
        return overrider.apply(methodCall).or(Suppliers.compose(defaultInterpreter, Suppliers.ofInstance(methodCall)));
    }
}
