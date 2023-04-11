package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.apply.ClusterManager;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TypeSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClusterManagerSimpleTest {

    private static final Matcher matcher = Matchers.getInstance().getMatcher();
    private final ClusterManager clusterManager = new ClusterManager(matcher);

    @BeforeAll
    public void init() throws URISyntaxException {
        URI testClusters = ClusterManagerSimpleTest.class
                .getClassLoader()
                .getResource("test_clusters")
                .toURI();
        clusterManager.initClusters(Arrays.asList(new File(testClusters).listFiles()));
    }

    @Test
    public void testTransform() {
        Tree block = new DefaultTree(TypeSet.type("Block"));
        Tree expression = new DefaultTree(TypeSet.type("ExpressionStatement"));
        Tree expr = new DefaultTree(TypeSet.type("MethodInvocation"));
        Tree receiver = new DefaultTree(TypeSet.type("METHOD_INVOCATION_RECEIVER"));
        Tree obj = new DefaultTree(TypeSet.type("SimpleName"), "mListView");
        Tree method = new DefaultTree(TypeSet.type("SimpleName"), "clearListeners");
        receiver.addChild(obj);
        expr.addChild(receiver);
        expr.addChild(method);
        expression.addChild(expr);
        block.addChild(expression);

        var resultList = clusterManager.ranking(block);
        for (var result : resultList) {
            System.out.println(result.transformed().toTreeString());
        }
    }
}
