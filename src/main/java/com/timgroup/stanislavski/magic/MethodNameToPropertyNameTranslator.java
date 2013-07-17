package com.timgroup.stanislavski.magic;

import static com.timgroup.karg.naming.TargetNameFormatter.LOWER_CAMEL_CASE;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.timgroup.karg.naming.TargetNameParser;
import com.timgroup.stanislavski.interpreters.AddressesProperty;
import com.timgroup.stanislavski.interpreters.AnnotationOverride;
import com.timgroup.stanislavski.interpreters.ExtractorFor;
import com.timgroup.stanislavski.magic.Patterns.One;
import com.timgroup.stanislavski.reflection.MethodCall;

public class MethodNameToPropertyNameTranslator implements Function<String, String> {

    public static String interpret(MethodCall methodCall) {
        return ExtractorFor.theMethodName()
                           .compose(new MethodNameToPropertyNameTranslator())
                           .chain(AnnotationOverride.<AddressesProperty, String>obtainingValueOf(AddressesProperty.class))
                           .apply(methodCall);
    }
    
    private static final List<Pattern> NO_UNDERSCORE_PATTERNS = 
        Patterns.matching(One.of("with", "having", "")
                             .followedByOneOf("An", "A", "The", "")
                             .followedByOneOf("([A-Z].*)Of", "([A-Z].*)"));
    
    private static final List<Pattern> UNDERSCORE_PATTERNS = 
        Patterns.matching(One.of("with_", "having_", "")
                             .followedByOneOf("an_", "a_", "the_", "")
                             .followedByOneOf("([a-z].*)_of", "([a-z].*)"));
    
    @Override
    public String apply(String methodName) {
        if (methodName.contains("_")) {
            return interpret(methodName,
                             UNDERSCORE_PATTERNS,
                             TargetNameParser.UNDERSCORE_SEPARATED);
        }
        
        return interpret(methodName,
                         NO_UNDERSCORE_PATTERNS,
                         TargetNameParser.CAMEL_CASE);
    }
    
    private String interpret(String methodName,
                             List<Pattern> patterns,
                             TargetNameParser parser) {
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
