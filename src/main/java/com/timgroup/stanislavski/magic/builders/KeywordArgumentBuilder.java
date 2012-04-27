package com.timgroup.stanislavski.magic.builders;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.timgroup.karg.keywords.typed.TypedKeyword;
import com.timgroup.karg.keywords.typed.TypedKeywordArgument;
import com.timgroup.karg.naming.TargetName;
import com.timgroup.karg.naming.TargetNameFormatter;

final class KeywordArgumentBuilder<T> implements Function<Map.Entry<String, Object>, TypedKeywordArgument<T>> {
    private final Class<T> recordType;

    KeywordArgumentBuilder(Class<T> recordType) {
        this.recordType = recordType;
    }

    @Override public TypedKeywordArgument<T> apply(Entry<String, Object> entry) {
        TargetName argName = TargetNameFormatter.LOWER_CAMEL_CASE.parse(entry.getKey());
        String keywordFieldName = argName.formatWith(TargetNameFormatter.UNDERSCORE_SEPARATED).toUpperCase();
        TypedKeyword<T, Object> typedKeyword = getKeyword(recordType, keywordFieldName);
        return typedKeyword.of(realise(entry.getValue()));
    }
    
    private Object realise(Object putativeValue) {
        if (putativeValue instanceof Supplier) {
            return ((Supplier<?>) putativeValue).get();
        }
        return putativeValue;
    }

    @SuppressWarnings("unchecked")
    private TypedKeyword<T, Object> getKeyword(final Class<T> recordType, String keywordFieldName) {
        try {
            return (TypedKeyword<T, Object>) recordType.getField(keywordFieldName).get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}