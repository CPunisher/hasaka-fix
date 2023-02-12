package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.antiunification.GTAntiUnifier;
import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.cpunisher.hasakafix.repo.Simple1;
import com.github.gumtreediff.tree.Tree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GTAntiUnifierTest {

    private static GTAntiUnifier antiUnifier;
    private static Tree oldTree;
    private static Tree newTree;

    @BeforeAll
    public static void init() {
        antiUnifier = new GTAntiUnifier();
        ISourceParser<Tree> parser = new GTSourceParser(".java");
        oldTree = parser.parse(Simple1.OLD_WORKER_DOT_JAVA);
        newTree = parser.parse(Simple1.NEW_WORKER_DOT_JAVA);
    }

    @Test
    public void testTreeToString() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method target = GTAntiUnifier.class.getDeclaredMethod("treeToString", Tree.class);
        target.setAccessible(true);

        String result1 = (String) target.invoke(antiUnifier, oldTree);
        String result2 = (String) target.invoke(antiUnifier, newTree);
        System.out.println(result1);
        System.out.println(result2);
        // TODO assert
    }

    @Test
    public void testAntiUnify() {
        String result = antiUnifier.antiUnify(oldTree.getChild("0.4.3"), newTree.getChild("0.4.3"));
        System.out.println(result);
    }
}
