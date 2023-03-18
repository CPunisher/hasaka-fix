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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class GTHierarchicalCalculatorTest {

    @Test
    void testCluster() {
        ISourceParser<TreeContext> parser = new GTSourceParser(".java");
        IEditor<Tree, GTTreeEdit> editor = new GTEditor();
        Set<GTTreeEdit> diff1 = editor.getEdits(parser.parse(Getafix.OLD_1_JAVA).getRoot(), parser.parse(Getafix.NEW_1_JAVA).getRoot());
        Set<GTTreeEdit> diff2 = editor.getEdits(parser.parse(Getafix.OLD_2_JAVA).getRoot(), parser.parse(Getafix.NEW_2_JAVA).getRoot());
        Set<GTTreeEdit> diff3 = editor.getEdits(parser.parse(Getafix.OLD_3_JAVA).getRoot(), parser.parse(Getafix.NEW_3_JAVA).getRoot());
        Set<GTTreeEdit> diff4 = editor.getEdits(parser.parse(Getafix.OLD_4_JAVA).getRoot(), parser.parse(Getafix.NEW_4_JAVA).getRoot());

        List<GTTreeEdit> edits = Stream.of(diff1.stream(), diff2.stream(), diff3.stream(), diff4.stream())
                .flatMap(Function.identity())
                .toList();
        IClusterCalculator<GTTreeEdit> cc = new GTHierarchicalCalculator(new PlainAntiUnifier2());
        var clusters = cc.cluster(edits);
        for (int i = 0; i < clusters.size(); i++) {
            printCluster(clusters.get(i), i);
        }
    }

    @Test
    public void testClusterByOrder() {
        ISourceParser<TreeContext> parser = new GTSourceParser(".java");
        IEditor<Tree, GTTreeEdit> editor = new GTEditor();
        Set<GTTreeEdit> diff1 = editor.getEdits(parser.parse(Getafix.OLD_1_JAVA).getRoot(), parser.parse(Getafix.NEW_1_JAVA).getRoot());
        Set<GTTreeEdit> diff2 = editor.getEdits(parser.parse(Getafix.OLD_2_JAVA).getRoot(), parser.parse(Getafix.NEW_2_JAVA).getRoot());
        Set<GTTreeEdit> diff3 = editor.getEdits(parser.parse(Getafix.OLD_3_JAVA).getRoot(), parser.parse(Getafix.NEW_3_JAVA).getRoot());
        Set<GTTreeEdit> diff4 = editor.getEdits(parser.parse(Getafix.OLD_4_JAVA).getRoot(), parser.parse(Getafix.NEW_4_JAVA).getRoot());

        List<GTTreeEdit> edits = Stream.of(diff1.stream(), diff2.stream(), diff3.stream(), diff4.stream())
                .flatMap(Function.identity())
                .toList();
        GTPlainAntiUnifier antiUnifier = new GTPlainAntiUnifier(new PlainAntiUnifier2());

        // Cluster
        List<GTTreeEdit> workList = new ArrayList<>(edits);
        GTTreeEdit current = workList.remove(0);
        Cluster<GTTreeEdit> last = new Cluster<>(current, List.of());
        printCluster(last, 0);
        while (!workList.isEmpty()) {
            current = workList.remove(0);
            var target = antiUnifier.antiUnify(last.getPattern(), current);
            last = new Cluster<>(target.template(), List.of());
            printCluster(last, 3 - workList.size());
        }
    }

    private void printCluster(Cluster<GTTreeEdit> cluster, int i) {
        System.out.println("Cluster #" + i + ":");
        System.out.println("Before: ");
        System.out.println(cluster.getPattern().before().toTreeString());
        System.out.println("After: ");
        System.out.println(cluster.getPattern().after().toTreeString());
    }
}
