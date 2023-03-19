package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.antiunification.GTPlainAntiUnifier;
import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.cluster.GTHierarchicalCalculator;
import com.cpunisher.hasakafix.cluster.IClusterCalculator;
import com.cpunisher.hasakafix.edit.editor.IEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.cpunisher.hasakafix.repo.Getafix;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

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

        GTHierarchicalCalculator cc = new GTHierarchicalCalculator(new PlainAntiUnifier2());
        // 1 and 2
        var cost12 = cc.cost(cluster1, cluster2);
        var cost13 = cc.cost(cluster1, cluster3);
        var cost14 = cc.cost(cluster1, cluster4);
        Assertions.assertEquals(cost12.dist(), Math.min(cost12.dist(), Math.min(cost13.dist(), cost14.dist())));
        // 3
        Cluster<GTTreeEdit> cluster12 = new Cluster<>(cost12.pattern(), List.of(cluster1, cluster2));
        var cost123 = cc.cost(cluster12, cluster3);
        var cost124 = cc.cost(cluster12, cluster4);
        Assertions.assertEquals(cost123.dist(), Math.min(cost123.dist(), cost124.dist()));
    }

    @Test
    public void testCluster() {
        IClusterCalculator<GTTreeEdit> cc = new GTHierarchicalCalculator(new PlainAntiUnifier2());
        Cluster<GTTreeEdit> cluster = cc.cluster(edits);
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
        }
    }

    private Cluster<GTTreeEdit> getWithChildren(Cluster<GTTreeEdit> cluster) {
        for (var child : cluster.children()) {
            if (!child.children().isEmpty()) {
                return child;
            }
        }
        return null;
    }

    private void printCluster(Cluster<GTTreeEdit> cluster, int i) {
        System.out.println("Cluster #" + i + ":");
        System.out.println("Before: ");
        System.out.println(cluster.pattern().before().toTreeString());
        System.out.println("After: ");
        System.out.println(cluster.pattern().after().toTreeString());
    }
}
