package com.cpunisher.hasakafix.tree;

import com.cpunisher.hasakafix.utils.Either;
import com.github.gumtreediff.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AUCompiler {
    public static AUTree fromString(String source) {
        AUTokenizer tokenizer = new AUTokenizer(source);
        AUTreeParser parser = new AUTreeParser(tokenizer);
        return parser.parse();
    }

    public static AUTree fromTree(Tree tree, Map<AUTree, Tree> ttMappings) {
        String value = tree.getLabel();
        String label = tree.getType().toString();
        List<Either<AUTree, AUHole>> children = new ArrayList<>();
        for (Tree child : tree.getChildren()) {
            children.add(Either.first(fromTree(child, ttMappings)));
        }
        AUTree auTree = new AUTree(label, value, children);
        ttMappings.put(auTree, tree);
        return auTree;
    }
}
