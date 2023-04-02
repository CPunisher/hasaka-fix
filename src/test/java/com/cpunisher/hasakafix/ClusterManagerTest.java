package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.apply.ClusterManager;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TypeSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClusterManagerTest {

    private ClusterManager manager = new ClusterManager(Matchers.getInstance().getMatcher());

    @BeforeAll
    public void init() {
        manager.initClusters(Arrays.asList(new File("./output/cluster").listFiles()));
    }

    @Test
    public void testRanking() {
        Tree methodInvocation = new DefaultTree(TypeSet.type("MethodInvocation"));
        Tree methodInvocationReceiver = new DefaultTree(TypeSet.type("METHOD_INVOCATION_RECEIVER"));
        Tree objName = new DefaultTree(TypeSet.type("SimpleName"), "task");
        Tree methodName = new DefaultTree(TypeSet.type("SimpleName"), "makeProgress");
        methodInvocation.addChild(methodInvocationReceiver);
        methodInvocationReceiver.addChild(objName);
        methodInvocation.addChild(methodName);

        List<Cluster<GTTreeEdit>> result = manager.ranking(methodInvocation);
        for (var cluster : result) {
            System.out.println("---------------------------------------");
            System.out.println(cluster.pattern().before().toTreeString());
        }
    }
}
