package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTEditor;
import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.cpunisher.hasakafix.repo.Simple1;
import com.cpunisher.hasakafix.utils.IdentityPair;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GTEditorTest {

    private static Tree oldTree;
    private static Tree newTree;
    private static MappingStore mappings;
    private static GTEditor.GTEditorInner editorInner;

    @BeforeAll
    public static void init() {
        ISourceParser<Tree> parser = new GTSourceParser(".java");
        oldTree = Objects.requireNonNull(parser.parse(Simple1.OLD_WORKER_DOT_JAVA));
        newTree = Objects.requireNonNull(parser.parse(Simple1.NEW_WORKER_DOT_JAVA));
        mappings = Matchers.getInstance().getMatcher().match(oldTree, newTree);
        editorInner = new GTEditor.GTEditorInner(oldTree, newTree);
    }

    @Test
    public void testGetEdits() {
        Set<GTTreeEdit> edits = new GTEditor().getEdits(oldTree, newTree);
        System.out.println(edits);
        assertEquals(2, edits.size());
    }

    @Test
    public void testGetConnectedActions() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method target = GTEditor.GTEditorInner.class.getDeclaredMethod("getConnectedActions");
        target.setAccessible(true);

        Set<Set<Action>> result = (Set<Set<Action>>) target.invoke(editorInner);
        System.out.println(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetEditPair() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method target = GTEditor.GTEditorInner.class.getDeclaredMethod("getEditPair", Tree.class);
        target.setAccessible(true);

        Tree modified = newTree.getChild("0.4.3.0");
        IdentityPair<Tree> result = (IdentityPair<Tree>) target.invoke(editorInner, modified);
        assertNotNull(result);
        assertNotNull(result.first);
        assertNotNull(result.second);
    }

    @Test
    public void testGetMaxUnmodified() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method target = GTEditor.GTEditorInner.class.getDeclaredMethod("getMaxUnmodified", Tree.class, Tree.class);
        target.setAccessible(true);

        Tree modified = newTree.getChild("0.4.3.0");
        IdentityPair<Tree> result = (IdentityPair<Tree>) target.invoke(editorInner, mappings.getSrcForDst(modified.getParent()), modified.getParent());
        assertNotNull(result);
        assertNotNull(result.first);
        assertNotNull(result.second);
    }
}
