package com.cpunisher.hasakafix.edit.editor.gumtree;

import com.cpunisher.hasakafix.edit.editor.IEditor;
import com.cpunisher.hasakafix.utils.IdentityPair;
import com.cpunisher.hasakafix.utils.disjointset.IDisjointSet;
import com.cpunisher.hasakafix.utils.disjointset.MapDisjointSet;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GTEditor implements IEditor<Tree, GTTreeEdit> {

    @Override
    public Set<GTTreeEdit> getEdits(Tree oldTree, Tree newTree) {
        GTEditorInner editor = new GTEditorInner(oldTree, newTree);
        return editor.getEdits();
    }

    public static class GTEditorInner {
        private final MappingStore mappings;

        public GTEditorInner(Tree oldTree, Tree newTree) {
            Matcher matcher = Matchers.getInstance().getMatcher();
            this.mappings = matcher.match(oldTree, newTree);
        }

        public Set<GTTreeEdit> getEdits() {
            Set<Set<Action>> groupedActions = getConnectedActions();
            List<Tree> roots = groupedActions.stream()
                    .map(actions -> actions.stream()
                            .min((action1, action2) -> {
                                // Outer is smaller
                                int posCmp = Integer.compare(action1.getNode().getPos(), action2.getNode().getPos());
                                if (posCmp != 0) return posCmp;
                                return Integer.compare(action2.getNode().getEndPos(), action1.getNode().getEndPos());
                            })
                            .orElseThrow()
                            .getNode()
                    )
                    .sorted((tree1, tree2) -> {
                        int posCmp = Integer.compare(tree1.getPos(), tree2.getPos());
                        if (posCmp != 0) return posCmp;
                        return Integer.compare(tree2.getEndPos(), tree1.getEndPos());
                    })
                    .toList();

            Set<GTTreeEdit> treeEdits = new HashSet<>();
            for (Tree root : roots) {
                IdentityPair<Tree> editPair = getEditPair(root);
                if (editPair == null) {
                    continue;
                }

                IdentityPair<Tree> maxUnmodified = getMaxUnmodified(editPair.first, editPair.second);
                treeEdits.add(new GTTreeEdit(maxUnmodified.first, maxUnmodified.second, mappings));
            }
            return treeEdits;
        }

        private Set<Set<Action>> getConnectedActions() {
            // Generate actions by GumTree
            EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
            EditScript editScript = editScriptGenerator.computeActions(mappings);
            List<Action> actions = editScript.asList();

            // Connect
            IDisjointSet<Action> connectedActions = new MapDisjointSet<>();
            actions.forEach(connectedActions::find);

            for (int i = 0; i < actions.size(); i++) {
                Action action1 = actions.get(i);
                for (int j = i + 1; j < actions.size(); j++) {
                    Action action2 = actions.get(j);
                    if (shouldConnect(action1, action2)) {
                        connectedActions.union(action1, action2);
                    }
                }
            }
            return connectedActions.findAll();
        }

        private boolean shouldConnect(Action action1, Action action2) {
            Tree parent1 = getParentOfAction(action1);
            Tree parent2 = getParentOfAction(action2);

            if (parent1.equals(parent2)) return true;
            return parent1.equals(action2.getNode()) || parent2.equals(action1.getNode());
        }

        private Tree getParentOfAction(Action action) {
            if (action instanceof Insert insert) return insert.getParent();
            if (action instanceof Update update) return update.getNode().getParent();
            if (action instanceof Delete delete) return delete.getNode().getParent();
            if (action instanceof TreeAddition treeAddition) return treeAddition.getParent();
            if (action instanceof TreeDelete treeDelete) return treeDelete.getNode().getParent();
            throw new IllegalStateException("unreachable");
        }

        @Nullable
        private IdentityPair<Tree> getEditPair(Tree root) {
            if (mappings.isDstMapped(root)) {
                return new IdentityPair<>(mappings.getSrcForDst(root), root);
            } else if (mappings.isSrcMapped(root)) {
                return new IdentityPair<>(root, mappings.getDstForSrc(root));
            }

            Tree parent = root.getParent();
            if (root.isRoot()) {
                return null;
            }

            // insertion or deletion
            if (mappings.isDstMapped(parent)) {
                return new IdentityPair<>(mappings.getSrcForDst(parent), parent);
            } else if (mappings.isSrcMapped(parent)) {
                return new IdentityPair<>(parent, mappings.getDstForSrc(parent));
            }
            return null;
        }

        private IdentityPair<Tree> getMaxUnmodified(Tree oldTree, Tree newTree) {
            List<Tree> pathOld = GTHelper.getPathFromRoot(oldTree);
            List<Tree> pathNew = GTHelper.getPathFromRoot(newTree);
            for (int i = 0; i < pathOld.size(); i++) {
                if (!mappings.has(pathOld.get(i), pathNew.get(i))) {
                    return new IdentityPair<>(pathOld.get(i - 1), pathNew.get(i - 1));
                }
            }
            return new IdentityPair<>(oldTree, newTree);
        }
    }
}
