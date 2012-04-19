package com.timgroup.stanislavski.magic;

import java.util.List;

import com.google.common.collect.Lists;

public final class Patterns {
    public static List<String> matching(Patterns.PatternGenerator generator) {
        return generator.generate();
    }
    
    public static interface PatternGenerator {
        List<String> generate();
    }
    
    public static class One implements Patterns.PatternGenerator {
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

        @Override public List<String> generate() {
            List<String> patterns = Lists.newLinkedList();
            generate(0, patterns, "");
            return patterns;
        }
        
        private void generate(int pos, List<String> patterns, String prefix) {
            if (pos >= choiceSets.size()) {
                patterns.add(prefix);
                return;
            }
            
            for (String choice : choiceSets.get(pos)) {
                generate(pos+1, patterns, prefix + choice);
            }
        }
    }
}