package com.cpunisher.hasakafix;

import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TypeSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import com.cpunisher.hasakafix.apply.ClusterManager;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;

import java.io.File;
import java.util.Arrays;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClusterManagerDataTest {

    private static final Matcher matcher = Matchers.getInstance().getMatcher();
    private final ClusterManager clusterManager = new ClusterManager(matcher);

    @BeforeAll
    public void init() {
        clusterManager.initClusters(Arrays.asList(new File("./output/clusters").listFiles()));
    }

    @Test
    public void testNullPointerException() {
        Tree expr = new DefaultTree(TypeSet.type("MethodInvocation"));
        Tree receiver = new DefaultTree(TypeSet.type("METHOD_INVOCATION_RECEIVER"));
        Tree obj = new DefaultTree(TypeSet.type("SimpleName"), "mListView");
        Tree method = new DefaultTree(TypeSet.type("SimpleName"), "clearListeners");
        receiver.addChild(obj);
        expr.addChild(receiver);
        expr.addChild(method);

        var rankingList = clusterManager.ranking(expr);
        for (int i = 0; i < rankingList.size(); i++) {
            System.out.println("------------------ Before " + i + " ------------------------");
            System.out.println(rankingList.get(i).cluster().pattern().before().toTreeString());
            System.out.println("------------------ After " + i + " ------------------------");
            System.out.println(rankingList.get(i).cluster().pattern().after().toTreeString());
        }
    }
}
