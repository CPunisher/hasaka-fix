package com.cpunisher.hasakafix.tree;

public class AUCompiler {
    public static AUTree compile(String source) {
        AUTokenizer tokenizer = new AUTokenizer(source);
        AUTreeParser parser = new AUTreeParser(tokenizer);
        return parser.parse();
    }
}
