package com.timgroup.stanislavski.interpreters;

import com.google.common.base.Preconditions;
import com.timgroup.stanislavski.reflection.MethodCall;

public final class ExtractorFor {

    public static Chainable<MethodCall, String> theMethodName() {
        return new Chainable<MethodCall, String>(new MethodCallInterpreter<String>() {
            @Override public String apply(MethodCall methodCall) {
                return methodCall.name();
            }
        });
    }
    
    public static Chainable<MethodCall, Object> theFirstArgument() {
        return new Chainable<MethodCall, Object>(new MethodCallInterpreter<Object>() {
            @Override public Object apply(MethodCall methodCall) {
                Preconditions.checkArgument(methodCall.arguments().size() == 1, "Incorrect number of arguments for method call %s", methodCall);
                return methodCall.firstArgument().value();
            }
        });
    }
}
