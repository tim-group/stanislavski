package com.timgroup.stanislavski.interpreters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.timgroup.stanislavski.reflection.MethodCall;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddressesProperty {
    public static Function<MethodCall, Optional<String>> OVERRIDER = AnnotationOverride.obtainingValueOf(AddressesProperty.class);
    String value();
}
