package com.timgroup.stanislavski.magic.builders;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.timgroup.karg.naming.TargetName;

import static com.timgroup.karg.naming.TargetNameFormatter.LOWER_CAMEL_CASE;

public class ViewRecord<T> implements InvocationHandler {

    private final Class<T> recordType;
    private final Map<Method, Object> methodMap;

    public ViewRecord(Class<T> recordType, Map<Method, Object> methodMap) {
        this.recordType = recordType;
        this.methodMap = methodMap;
    }

    @SuppressWarnings("unchecked")
    public static <T> T proxying(Class<T> recordType, Map<String, Object> properties) {
        Map<Method, Object> methodMap = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Method method = findMethod(recordType, entry.getKey());
            methodMap.put(method, entry.getValue());
        }
        return (T) Proxy.newProxyInstance(recordType.getClassLoader(), new Class<?>[] {recordType}, new ViewRecord<T>(recordType, methodMap));
    }
    
    private static <T> Method findMethod(Class<T> recordType, String propertyName) {
        return Iterables.find(Lists.newArrayList(recordType.getMethods()), matchingPropertyName(propertyName));
    }
    
    private static Predicate<Method> matchingPropertyName(String propertyName) {
        final TargetName targetName = LOWER_CAMEL_CASE.parse(propertyName);
        return new Predicate<Method>() {
            @Override public boolean apply(Method method) {
                String methodName = method.getName();
                return methodName.equals(targetName.formatWith(LOWER_CAMEL_CASE)) ||
                        methodName.equals(targetName.prefixedWith("get").formatWith(LOWER_CAMEL_CASE)) ||
                        methodName.equals(targetName.prefixedWith("is").formatWith(LOWER_CAMEL_CASE));
            }
        };
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("equals")) {
            return testForEquality(args[0]);
        }
        
        if (method.getName().equals("hashCode")) {
            return methodMap.hashCode();
        }
        
        if (method.getName().equals("toString")) {
            return stringRepresentation();
        }
        
        return methodMap.get(method);
    }

    private Object stringRepresentation() {
        return Joiner.on(", ").join(Iterables.transform(methodMap.entrySet(), entryToString));
    }
    
    private static final Function<Map.Entry<Method, Object>, String> entryToString = new Function<Map.Entry<Method,Object>, String>() {
        @Override public String apply(Entry<Method, Object> entry) {
            return String.format("%s: %s", entry.getKey().getName(), entry.getValue());
        }
    };

    private Object testForEquality(Object object) {
        if (!recordType.isAssignableFrom(object.getClass())) {
            return false;
        }
        
        for (Map.Entry<Method, Object> entry : methodMap.entrySet()) {
            if (!safeEquals(otherProperty(object, entry), entry.getValue())) {
                return false;
            }
        }
        
        return true;
    }

    private boolean safeEquals(Object otherValue, Object value) {
        if (otherValue == null) {
            return value == null;
        }
        return otherValue.equals(value);
    }

    private Object otherProperty(Object object, Map.Entry<Method, Object> entry) {
        try {
            return entry.getKey().invoke(object);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
