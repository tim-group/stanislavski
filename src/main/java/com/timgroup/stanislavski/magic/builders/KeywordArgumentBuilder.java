package com.timgroup.stanislavski.magic.builders;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.timgroup.karg.keywords.typed.TypedKeyword;
import com.timgroup.karg.keywords.typed.TypedKeywordArgument;
import com.timgroup.karg.naming.TargetName;
import com.timgroup.karg.naming.TargetNameFormatter;
import com.timgroup.karg.naming.TargetNameParser;

final class KeywordArgumentBuilder<T> implements Function<Map.Entry<String, Object>, TypedKeywordArgument<T>> {
    private final Class<T> recordType;

    KeywordArgumentBuilder(Class<T> recordType) {
        this.recordType = recordType;
    }

    @Override public TypedKeywordArgument<T> apply(Entry<String, Object> entry) {
        TargetName argName = TargetNameParser.CAMEL_CASE.parse(entry.getKey());
        String keywordFieldName = argName.formatWith(TargetNameFormatter.UNDERSCORE_SEPARATED).toUpperCase();
        TypedKeyword<T, Object> typedKeyword = getKeyword(recordType, keywordFieldName);
        return typedKeyword.of(entry.getValue());
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