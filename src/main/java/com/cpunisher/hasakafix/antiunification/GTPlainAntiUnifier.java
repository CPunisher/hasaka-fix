package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifyData;
import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.ImmutableTree;
import com.github.gumtreediff.tree.Tree;

import java.util.*;

public class GTPlainAntiUnifier {

    private final IAntiUnifier<Tree> plainAntiUnifier;

    public GTPlainAntiUnifier(IAntiUnifier<Tree> plainAntiUnifier) {
        this.plainAntiUnifier = plainAntiUnifier;
    }

    public GTAntiUnifierData antiUnify(GTTreeEdit left, GTTreeEdit right) {
        GTTreeMappingHelper leftHelper = new GTTreeMappingHelper(left);
        GTTreeMappingHelper rightHelper = new GTTreeMappingHelper(right);

        var beforeData = plainAntiUnifier.antiUnify(leftHelper.stripUnmodified(left.before()), rightHelper.stripUnmodified(right.before())).get(0);
        var afterData = plainAntiUnifier.antiUnify(leftHelper.stripUnmodified(left.after()), rightHelper.stripUnmodified(right.after())).get(0);

        Tree before = beforeData.template();
        Tree after = afterData.template();
        // populate back, TODO optimize
        Set<Tree> leftUnmodified = new HashSet<>(left.before().getChildren());
        Set<Tree> rightUnmodified = new HashSet<>(right.after().getChildren());
        leftUnmodified.removeAll(leftHelper.modified);
        rightUnmodified.removeAll(rightHelper.modified);
        for (var iter = leftUnmodified.iterator(); iter.hasNext();) {
            Tree lc = iter.next();
            for (var iter2 = rightUnmodified.iterator(); iter2.hasNext();) {
                Tree rc = iter2.next();
                Tree au = plainAntiUnifier.antiUnify(lc, rc).get(0).template();
                if (!Objects.equals(au.getLabel(), PlainAntiUnifier2.HOLE_LABEL)) {
                    int beforePos = Math.min(left.before().getChildPosition(lc), before.getChildren().size());
                    int afterPos = Math.min(right.after().getChildPosition(rc), after.getChildren().size());
                    Tree auCopy1 = au.deepCopy();
                    before.getChildren().add(beforePos, auCopy1);
                    auCopy1.setParent(before);
                    Tree auCopy2 = au.deepCopy();
                    after.getChildren().add(afterPos, auCopy2);
                    auCopy2.setParent(after);
                    iter.remove();
                    iter2.remove();
                    break;
                }
            }
        }
        return new GTAntiUnifierData(
                new GTTreeEdit(before, after, Matchers.getInstance().getMatcher().match(before, after)),
                beforeData.substitutions(),
                afterData.substitutions()
        );
    }

    public static class GTTreeMappingHelper {
        private final Set<Tree> modified = new HashSet<>();

        private GTTreeMappingHelper(GTTreeEdit edit) {
            EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
            EditScript editScript = editScriptGenerator.computeActions(edit.mappings());
            for (Action action : editScript.asList()) {
                modified.add(action.getNode());
                if (edit.mappings().isSrcMapped(action.getNode())) modified.add(edit.mappings().getDstForSrc(action.getNode()));
                if (edit.mappings().isDstMapped(action.getNode())) modified.add(edit.mappings().getSrcForDst(action.getNode()));
            }
        }

        private Tree stripUnmodified(Tree origin) {
            if (origin.isLeaf()) {
                return origin.deepCopy();
            }

            Tree newTree = new DefaultTree(origin.getType(), origin.getLabel());
            boolean hasModified = origin.getChildren().stream().anyMatch(modified::contains);
            if (hasModified) {
                for (var child : origin.getChildren()) {
                    if (modified.contains(child)) {
                        newTree.addChild(child.deepCopy());
                    }
                }
            } else {
                for (var child : origin.getChildren()) {
                    newTree.addChild(stripUnmodified(child));
                }
            }
            return newTree;
        }
    }

    public record GTAntiUnifierData(GTTreeEdit template, List<AntiUnifySubstitution<Tree>> beforeSubs, List<AntiUnifySubstitution<Tree>> afterSubs) {}
}
