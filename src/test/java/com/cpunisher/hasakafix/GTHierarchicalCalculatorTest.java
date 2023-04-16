package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.antiunification.GTPlainAntiUnifier;
import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.cluster.GTCostCalculator;
import com.cpunisher.hasakafix.cluster.GTHierarchicalCalculator;
import com.cpunisher.hasakafix.edit.editor.IEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.cpunisher.hasakafix.repo.Getafix;
import com.cpunisher.hasakafix.utils.XmlHelper;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.tree.DefaultTree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TypeSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GTHierarchicalCalculatorTest {

    private List<GTTreeEdit> edits;

    @BeforeAll
    public void init() {
        ISourceParser<Tree> parser = new GTSourceParser(".java");
        IEditor<Tree, GTTreeEdit> editor = new GTEditor();
        Set<GTTreeEdit> diff1 = editor.getEdits(parser.parse(Getafix.OLD_1_JAVA), parser.parse(Getafix.NEW_1_JAVA));
        Set<GTTreeEdit> diff2 = editor.getEdits(parser.parse(Getafix.OLD_2_JAVA), parser.parse(Getafix.NEW_2_JAVA));
        Set<GTTreeEdit> diff3 = editor.getEdits(parser.parse(Getafix.OLD_3_JAVA), parser.parse(Getafix.NEW_3_JAVA));
        Set<GTTreeEdit> diff4 = editor.getEdits(parser.parse(Getafix.OLD_4_JAVA), parser.parse(Getafix.NEW_4_JAVA));
        edits = Stream.of(diff1.stream(), diff2.stream(), diff3.stream(), diff4.stream())
                .flatMap(Function.identity())
                .toList();
    }

    @Test
    public void testCost() {
        Cluster<GTTreeEdit> cluster1 = new Cluster<>(edits.get(0), Collections.emptyList());
        Cluster<GTTreeEdit> cluster2 = new Cluster<>(edits.get(1), Collections.emptyList());
        Cluster<GTTreeEdit> cluster3 = new Cluster<>(edits.get(2), Collections.emptyList());
        Cluster<GTTreeEdit> cluster4 = new Cluster<>(edits.get(3), Collections.emptyList());

        GTPlainAntiUnifier antiUnifier = new GTPlainAntiUnifier(new PlainAntiUnifier2());
        GTCostCalculator cc = new GTCostCalculator();
        // 1 and 2
        var cost12 = cc.cost(antiUnifier.antiUnify(cluster1.pattern(), cluster2.pattern()));
        var cost13 = cc.cost(antiUnifier.antiUnify(cluster1.pattern(), cluster3.pattern()));
        var cost14 = cc.cost(antiUnifier.antiUnify(cluster1.pattern(), cluster4.pattern()));
        Assertions.assertEquals(cost12.dist(), Math.min(cost12.dist(), Math.min(cost13.dist(), cost14.dist())));
        // 3
        Cluster<GTTreeEdit> cluster12 = new Cluster<>(cost12.pattern(), List.of(cluster1, cluster2));
        var cost123 = cc.cost(antiUnifier.antiUnify(cluster12.pattern(), cluster3.pattern()));
        var cost124 = cc.cost(antiUnifier.antiUnify(cluster12.pattern(), cluster4.pattern()));
        Assertions.assertEquals(cost123.dist(), Math.min(cost123.dist(), cost124.dist()));
    }

    @Test
    public void testCluster() {
        GTHierarchicalCalculator cc = new GTHierarchicalCalculator(new PlainAntiUnifier2());
        List<Cluster<GTTreeEdit>> clusterList = cc.cluster(edits);
        Assertions.assertEquals(1, clusterList.size());

        Cluster<GTTreeEdit> cluster = clusterList.get(0);
        Assertions.assertTrue(cluster.children().stream().anyMatch(c -> c.pattern().equals(edits.get(3))));
        Assertions.assertTrue(getWithChildren(cluster).children().stream().anyMatch(c -> c.pattern().equals(edits.get(2))));
        Assertions.assertTrue(getWithChildren(getWithChildren(cluster)).children().stream().anyMatch(c -> c.pattern().equals(edits.get(1))));

        int count = 0;
        while (cluster != null) {
            printCluster(cluster, ++count);
            cluster = getWithChildren(cluster);
            System.out.println("-".repeat(10));
        }
    }

    @Test
    public void testClusterByOrder() {
        GTPlainAntiUnifier antiUnifier = new GTPlainAntiUnifier(new PlainAntiUnifier2());

        // Cluster
        List<GTTreeEdit> workList = new ArrayList<>(edits);
        GTTreeEdit current = workList.remove(0);
        Cluster<GTTreeEdit> last = new Cluster<>(current, Collections.emptyList());
        printCluster(last, 0);
        while (!workList.isEmpty()) {
            current = workList.remove(0);
            var target = antiUnifier.antiUnify(last.pattern(), current);
            last = new Cluster<>(target.template(), List.of(last, new Cluster<>(current, Collections.emptyList())));
            printCluster(last, 3 - workList.size());
            // for test
//            printXml(current.before());
//            printXml(current.after());
        }
    }

    @Test
    public void testTestResult() {
        GTHierarchicalCalculator calculator = new GTHierarchicalCalculator(new PlainAntiUnifier2());
        Tree before = new DefaultTree(TypeSet.type("MethodInvocation"));
        Tree receiver = new DefaultTree(TypeSet.type("METHOD_INVOCATION_RECEIVER"));
        Tree hole1 = new DefaultTree(TypeSet.type("?"), "#HOLE_1");
        Tree hole2 = new DefaultTree(TypeSet.type("?"), "#HOLE_2");
        receiver.addChild(hole1);
        before.addChild(receiver);
        before.addChild(hole2);
        Tree after = new DefaultTree(TypeSet.type("MethodInvocation"), "#HOLE_0");
        Assertions.assertFalse(calculator.testResult(new GTTreeEdit(before, after, null)));
    }

    private Cluster<GTTreeEdit> getWithChildren(Cluster<GTTreeEdit> cluster) {
        for (var child : cluster.children()) {
            if (!child.children().isEmpty()) {
                return child;
            }
        }
        return null;
    }

    private void printXml(Tree tree) {
        try {
            StringWriter stringWriter = new StringWriter();
            TreeIoUtils.toXml(XmlHelper.toTreeContext(tree)).writeTo(stringWriter);
            System.out.println(stringWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void printCluster(Cluster<GTTreeEdit> cluster, int i) {
        System.out.println("Cluster #" + i + ":");
        System.out.println("Before: ");
        System.out.println(cluster.pattern().before().toTreeString());
        System.out.println("After: ");
        System.out.println(cluster.pattern().after().toTreeString());
    }
}
