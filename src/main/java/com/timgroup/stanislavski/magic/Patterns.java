package com.timgroup.stanislavski.magic;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

final class Patterns {
    public static List<Pattern> matching(PatternGenerator generator) {
        return generator.generate();
    }
    
    public static interface PatternGenerator {
        List<Pattern> generate();
    }
    
    public static class One implements PatternGenerator {
        public static Patterns.One of(String...choices) {
            return new One(choices);
        }
        
        private final List<String[]> choiceSets = Lists.newLinkedList();
        
        public One(String[] choices) {
            choiceSets.add(choices);
        }
        
        public Patterns.One followedByOneOf(String...choices) {
            choiceSets.add(choices);
            return this;
        }

        @Override public List<Pattern> generate() {
            List<Pattern> patterns = Lists.newLinkedList();
            generate(0, patterns, "");
            return patterns;
        }
        
        private void generate(int pos, List<Pattern> patterns, String prefix) {
            if (pos >= choiceSets.size()) {
                patterns.add(Pattern.compile(prefix));
                return;
            }
            
            for (String choice : choiceSets.get(pos)) {
                generate(pos+1, patterns, prefix + choice);
            }
        }
    }
}