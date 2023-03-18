package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.cpunisher.hasakafix.tree.AUHole;
import com.cpunisher.hasakafix.tree.AUTree;
import com.cpunisher.hasakafix.utils.Either;

import java.util.ArrayList;
import java.util.List;

public class PlainAntiUnifier implements IAntiUnifier<Either<AUTree, AUHole>> {
    @Override
    public List<AntiUnifyData<Either<AUTree, AUHole>>> antiUnify(Either<AUTree, AUHole> left, Either<AUTree, AUHole> right) {
        return List.of(_antiUnify(left, right));
    }

    private AntiUnifyData<Either<AUTree, AUHole>> _antiUnify(Either<AUTree, AUHole> left, Either<AUTree, AUHole> right) {
        // One of theme is a hole
        if (left.isSecond() || right.isSecond()) {
            var beforeLabel = left.isSecond() ? left.getSecond().label() : left.getFirst().label();
            var afterLabel = right.isSecond() ? right.getSecond().label() : right.getFirst().label();
            Either<AUTree, AUHole> sub;
            if (beforeLabel.equals(afterLabel)) {
                sub = Either.second(new AUHole(beforeLabel));
            } else {
                sub = Either.second(new AUHole("?"));
            }
            return new AntiUnifyData<>(sub, List.of(new AntiUnifySubstitution<>(sub, left, right)));
        }

        AUTree beforeTree = left.getFirst();
        AUTree afterTree = right.getFirst();
        if (beforeTree.label().equals(afterTree.label())
                && beforeTree.value().equals(afterTree.value())
                && beforeTree.children().size() == afterTree.children().size()
        ) {
            List<Either<AUTree, AUHole>> children = new ArrayList<>();
            List<AntiUnifySubstitution<Either<AUTree, AUHole>>> substitutions = new ArrayList<>();
            for (int i = 0; i < beforeTree.children().size(); i++) {
                AntiUnifyData<Either<AUTree, AUHole>> result = _antiUnify(beforeTree.children().get(i), afterTree.children().get(i));
                children.add(result.template());
                substitutions.addAll(result.substitutions());
            }
            return new AntiUnifyData<>(Either.first(new AUTree(beforeTree.label(), beforeTree.value(), children)), substitutions);
        }
        Either<AUTree, AUHole> sub = Either.second(new AUHole("?"));
        return new AntiUnifyData<>(sub, List.of(new AntiUnifySubstitution<>(sub, left, right)));
    }
}
