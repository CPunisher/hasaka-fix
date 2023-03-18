package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.Type;
import com.github.gumtreediff.tree.TypeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlainAntiUnifier2 implements IAntiUnifier<Tree> {

    public static final Type HOLE_TYPE = TypeSet.type("?");
    public static final String HOLE_LABEL = "#HOLE#";

    @Override
    public List<AntiUnifyData<Tree>> antiUnify(Tree left, Tree right) {
        return List.of(_antiUnify(left, right));
    }

    private AntiUnifyData<Tree> _antiUnify(Tree left, Tree right) {
        // One of theme is a hole
        if (Objects.equals(left.getLabel(), HOLE_LABEL) || Objects.equals(right.getLabel(), HOLE_LABEL)) {
            Tree sub;
            if (left.hasSameType(right)) {
                sub = new DefaultTree(left.getType(), HOLE_LABEL);
            } else {
                sub = new DefaultTree(HOLE_TYPE, HOLE_LABEL);
            }
            return new AntiUnifyData<>(sub, List.of(new AntiUnifySubstitution<>(sub, left, right)));
        }

        if (left.hasSameTypeAndLabel(right) && left.getChildren().size() == right.getChildren().size()
        ) {
            Tree newTree = new DefaultTree(left.getType(), left.getLabel());
            List<AntiUnifySubstitution<Tree>> substitutions = new ArrayList<>();
            for (int i = 0; i < left.getChildren().size(); i++) {
                AntiUnifyData<Tree> result = _antiUnify(left.getChild(i), right.getChild(i));
                newTree.addChild(result.template());
                substitutions.addAll(result.substitutions());
            }
            return new AntiUnifyData<>(newTree, substitutions);
        } else if (left.hasSameType(right)) {
            Tree hole = new DefaultTree(left.getType(), HOLE_LABEL);
            return new AntiUnifyData<>(hole, List.of(new AntiUnifySubstitution<>(hole, left, right)));
        }
        Tree hole = new DefaultTree(HOLE_TYPE, HOLE_LABEL);
        return new AntiUnifyData<>(hole, List.of(new AntiUnifySubstitution<>(hole, left, right)));
    }
}
