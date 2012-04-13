package com.timgroup.stanislavski.interpreters;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Suppliers;

public final class Chainable<S, T> implements Function<S, T> {
    private final Function<S, T> inner;
    
    public Chainable(Function<S, T> inner) {
        this.inner = inner;
    }
    
    @Override public T apply(S arg) {
        return inner.apply(arg);
    }
    
    public <T2> Chainable<S, T2> compose(Function<T, T2> f) {
        return new Chainable<S, T2>(Functions.compose(f, inner));
    }
    
    public Chainable<S, T> chain(final Function<S, Optional<T>> f) {
        return new Chainable<S, T>(new Function<S, T>() {
            @Override public T apply(S arg0) {
                return f.apply(arg0).or(Suppliers.compose(inner, Suppliers.ofInstance(arg0)));
            }
        });
    }
}