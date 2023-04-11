package com.cpunisher.hasakafix.antiunification;

import com.cpunisher.hasakafix.antiunification.bean.AntiUnifySubstitution;
import com.cpunisher.hasakafix.cluster.GTCostCalculator;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;

import java.util.*;
import java.util.stream.Collectors;

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
        var beforeChildrenSize  = left.before().getChildren().size() + right.before().getChildren().size();
        var afterChildrenSize  = left.after().getChildren().size() + right.after().getChildren().size();
        var leftDelegation = beforeChildrenSize > afterChildrenSize ? left.before() : left.after();
        var rightDelegation = beforeChildrenSize > afterChildrenSize ? right.before() : right.after();
        var leftSplits = split(leftDelegation.getChildren(), leftHelper.edit.modified());
        var rightSplits = split(rightDelegation.getChildren(), rightHelper.edit.modified());

        List<AntiUnifySubstitution<Tree>> substitutionList = new ArrayList<>();
        Map<Tree, List<Tree>> copiedTreeMap = new HashMap<>();
        for (int i = 0; i < leftSplits.size() && i < rightSplits.size(); i++) {
            var leftUnmodified = leftSplits.get(i);
            var rightUnmodified = rightSplits.get(i);
            for (var iter = leftUnmodified.iterator(); iter.hasNext(); ) {
                Tree lc = iter.next();
                for (var iter2 = rightUnmodified.iterator(); iter2.hasNext(); ) {
                    Tree rc = iter2.next();
                    var au = plainAntiUnifier.antiUnify(lc, rc).get(0);
                    if (!au.template().getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL)) {
                        int beforePos = Math.min(leftDelegation.getChildPosition(lc), before.getChildren().size());
                        int afterPos = Math.min(rightDelegation.getChildPosition(rc), after.getChildren().size());
                        Tree auCopy1 = deepCloneWithMap(au.template(), copiedTreeMap);
                        before.getChildren().add(beforePos, auCopy1);
                        auCopy1.setParent(before);
                        Tree auCopy2 = deepCloneWithMap(au.template(), copiedTreeMap);
                        after.getChildren().add(afterPos, auCopy2);
                        auCopy2.setParent(after);
                        iter.remove();
                        iter2.remove();
                        substitutionList.addAll(au.substitutions());
                        break;
                    }
                }
            }
        }

        // Merge holes with same left and right substitutions
        var holesGroup = groupSubstitution(List.of(beforeData.substitutions(), afterData.substitutions(), substitutionList));
        for (int i = 0; i < holesGroup.size(); i++) {
            for (var hole : holesGroup.get(i)) {
                if (copiedTreeMap.containsKey(hole.substitution())) {
                    for (var copiedHole : copiedTreeMap.get(hole.substitution())) {
                        copiedHole.setLabel(PlainAntiUnifier2.HOLE_LABEL + "_" + i);
                    }
                } else {
                    hole.substitution().setLabel(PlainAntiUnifier2.HOLE_LABEL + "_" + i);
                }
            }
        }

        return new GTAntiUnifierData(
                new GTTreeEdit(before, after, Matchers.getInstance().getMatcher().match(before, after)),
                groupSubstitution(List.of(beforeData.substitutions(), substitutionList)).stream().map(list -> list.get(0)).toList(),
                groupSubstitution(List.of(afterData.substitutions(), substitutionList)).stream().map(list -> list.get(0)).toList()
        );
    }

    private static List<List<Tree>> split(List<Tree> trees, Set<Tree> separators) {
        List<List<Tree>> result = new ArrayList<>();
        List<Tree> current = new ArrayList<>();
        for (int i = 0; i < trees.size(); i++) {
            var tree = trees.get(i);
            if (separators.contains(tree)) {
                if (current.size() > 0 || i == 0) {
                    result.add(current);
                    current = new ArrayList<>();
                }
            } else {
                current.add(tree);
            }
        }
        if (current.size() > 0) {
            result.add(current);
        }
        return result;
    }

    private static List<List<AntiUnifySubstitution<Tree>>> groupSubstitution(List<List<AntiUnifySubstitution<Tree>>> lists) {
        var values = lists.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(substitution -> new SubstitutionPair(substitution.left(), substitution.right())))
                .values();
        return new ArrayList<>(values);
    }

    private static Tree deepCloneWithMap(Tree tree, Map<Tree, List<Tree>> map) {
        Tree copy = new DefaultTree(tree.getType(), tree.getLabel());
        copy.setPos(tree.getPos());
        copy.setLength(tree.getLength());

        if (tree.getLabel().startsWith(PlainAntiUnifier2.HOLE_LABEL)) {
            map.computeIfAbsent(tree, t -> new ArrayList<>()).add(copy);
        }
        for (Tree child : tree.getChildren())
            copy.addChild(deepCloneWithMap(child, map));
        return copy;
    }

    public static class GTTreeMappingHelper {
        private final GTTreeEdit edit;

        private GTTreeMappingHelper(GTTreeEdit edit) {
            this.edit = edit;
            if (edit.modified() == null) {
                edit.modified(new HashSet<>());
                EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
                EditScript editScript = editScriptGenerator.computeActions(edit.mappings());
                for (Action action : editScript.asList()) {
                    edit.modified().add(action.getNode());
                    if (edit.mappings().isSrcMapped(action.getNode()))
                        edit.modified().add(edit.mappings().getDstForSrc(action.getNode()));
                    if (edit.mappings().isDstMapped(action.getNode()))
                        edit.modified().add(edit.mappings().getSrcForDst(action.getNode()));
                }
            }
        }

        private Tree stripUnmodified(Tree origin) {
            if (origin.isLeaf()) {
                return origin.deepCopy();
            }

            Tree newTree = new DefaultTree(origin.getType(), origin.getLabel());
            boolean allModified = edit.modified().containsAll(origin.getChildren());
            if (!allModified) {
                for (var child : origin.getChildren()) {

                    if (edit.modified().contains(child)) {
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

    public record GTAntiUnifierData(GTTreeEdit template, List<AntiUnifySubstitution<Tree>> beforeSubs,
                                    List<AntiUnifySubstitution<Tree>> afterSubs) {
    }

    private record SubstitutionPair(Tree before, Tree after) {
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj instanceof SubstitutionPair target) {
                return treeEquals(this.before, target.before) && treeEquals(this.after, target.after);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(before.getMetrics().hash, after.getMetrics().hash);
        }

        public static boolean treeEquals(Tree tree1, Tree tree2) {
            if (Objects.equals(tree1, tree2)) return true;

            var children1 = tree1.getChildren();
            var children2 = tree2.getChildren();
            if (tree1.hasSameTypeAndLabel(tree2) && children1.size() == children2.size()) {
                for (int i = 0; i < children1.size(); i++) {
                    if (!treeEquals(children1.get(i), children2.get(i))) {
                        return false;
                    }
                }
            }
            return true;
        }

    }
}
