package com.cpunisher.hasakafix.apply;

import com.github.gumtreediff.tree.Tree;

public interface ITreeMatcher {
    boolean match(Tree pattern, Tree tree);
}
