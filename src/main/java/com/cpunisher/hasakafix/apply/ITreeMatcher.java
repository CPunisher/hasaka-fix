package com.cpunisher.hasakafix.apply;

import com.github.gumtreediff.tree.Tree;

import java.util.Map;
import java.util.Optional;

public interface ITreeMatcher {
    Optional<Map<String, Tree>> match(Tree pattern, Tree tree);
}
