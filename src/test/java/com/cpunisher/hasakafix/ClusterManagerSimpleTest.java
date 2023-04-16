package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.apply.ClusterManager;
import com.cpunisher.hasakafix.apply.CodeGen;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TypeSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
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
        clusterManager.initClusterFiles(Arrays.asList(new File(testClusters).listFiles()));
    }

    @Test
    public void testTransform() throws IOException {
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

        find(block);
    }

    private void find(Tree tree) throws IOException {
        var rankingList = clusterManager.ranking(tree);
        for (int i = 0; i < rankingList.size(); i++) {
            var matchResult = rankingList.get(i);
            System.out.println("------------------ Before " + i + " ------------------------");
            System.out.println(rankingList.get(i).cluster().pattern().before().toTreeString());
            System.out.println(CodeGen.generate(tree));
            System.out.println("------------------ After " + i + " ------------------------");
            System.out.println(rankingList.get(i).cluster().pattern().after().toTreeString());
            System.out.println(CodeGen.generate(matchResult.after()));
        }
    }
}
