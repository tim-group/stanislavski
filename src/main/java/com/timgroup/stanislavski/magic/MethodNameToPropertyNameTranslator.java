package com.timgroup.stanislavski.magic;

import static com.timgroup.karg.naming.TargetNameFormatter.LOWER_CAMEL_CASE;
import static com.timgroup.karg.naming.TargetNameFormatter.UNDERSCORE_SEPARATED;
import static com.timgroup.karg.naming.TargetNameFormatter.UPPER_CAMEL_CASE;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.timgroup.karg.naming.TargetNameFormatter;
import com.timgroup.stanislavski.interpreters.Alias;
import com.timgroup.stanislavski.interpreters.ExtractorFor;
import com.timgroup.stanislavski.magic.Patterns.One;
import com.timgroup.stanislavski.reflection.MethodCall;

public class MethodNameToPropertyNameTranslator implements Function<String, String> {

    public static String interpret(MethodCall methodCall) {
        return ExtractorFor.theMethodName()
                           .compose(new MethodNameToPropertyNameTranslator())
                           .chain(Alias.OVERRIDER)
                           .apply(methodCall);
    }
    
    private static final List<Pattern> NO_UNDERSCORE_PATTERNS = 
        Patterns.matching(One.of("with", "having")
                             .followedByOneOf("An", "A", "The", "")
                             .followedByOneOf("([A-Z].*)Of", "([A-Z].*)"));
    
    private static final List<Pattern> UNDERSCORE_PATTERNS = 
        Patterns.matching(One.of("with_", "having_")
                             .followedByOneOf("an_", "a_", "the_")
                             .followedByOneOf("([a-z].*)_of", "([a-z].*)"));
    
    @Override
    public String apply(String methodName) {
        if (methodName.contains("_")) {
            return interpret(methodName,
                             UNDERSCORE_PATTERNS,
                             UNDERSCORE_SEPARATED);
        }
        
        return interpret(methodName,
                         NO_UNDERSCORE_PATTERNS,
                         UPPER_CAMEL_CASE);
    }
    
    private String interpret(String methodName,
                             List<Pattern> patterns,
                             TargetNameFormatter parser) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(methodName);
            if (matcher.matches()) {
                return parser.parse(matcher.group(1))
                             .formatWith(LOWER_CAMEL_CASE);
            }
        }
        return methodName;
    }
}
