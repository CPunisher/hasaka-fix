package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.apply.ClusterManager;
import com.cpunisher.hasakafix.apply.MatchResult;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.cluster.GTHierarchicalCalculator;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.utils.tree.GTTreeUtils;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CommandLine.Command(name = "bench", description = "10-fold cross validation")
public class CommandBench implements Runnable {
    @CommandLine.Parameters(paramLabel = "<10-dirs>")
    List<File> groupDirs = new ArrayList<>();

    @Override
    public void run() {
        if (groupDirs.size() != 10 ||
                !groupDirs.stream().allMatch(dir -> dir.exists() && dir.isDirectory())) {
            return;
        }
        System.out.println("Loading files...");
        List<List<GTTreeEdit>> editGroups = new ArrayList<>();
        Matcher matcher = Matchers.getInstance().getMatcher();
        for (var dir : groupDirs) {
            List<GTTreeEdit> edits = new ArrayList<>();
            for (var commit : Objects.requireNonNull(dir.listFiles())) {
                if (!commit.exists() || !commit.isDirectory()) {
                    continue;
                }
                int count = Objects.requireNonNull(commit.list()).length / 2;
                for (int i = 0 ; i < count; i++) {
                    Path before = commit.toPath().resolve("before_" + i + ".xml");
                    Path after = commit.toPath().resolve("after_" + i + ".xml");
                    try {
                        Tree beforeTree = TreeIoUtils.fromXml().generateFrom().file(before).getRoot();
                        Tree afterTree = TreeIoUtils.fromXml().generateFrom().file(after).getRoot();
                        var mapping = matcher.match(beforeTree, afterTree);
                        edits.add(new GTTreeEdit(beforeTree, afterTree, mapping));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            editGroups.add(edits);
        }
        System.out.println("10-fold cross validation");
        BenchResult totalResult = new BenchResult(0, 0, 0, 0, new ArrayList<>());
        for (int i = 0; i < 10; i++) {
            System.out.println("============ Start batching " + (i + 1) + " ============ ");
            List<GTTreeEdit> train = new ArrayList<>();
            List<GTTreeEdit> test = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                if (i == j) test.addAll(editGroups.get(j));
                else train.addAll(editGroups.get(j));
            }
            BenchResult benchResult = batch(train, test);
            totalResult = totalResult.plus(benchResult);
            System.out.println(benchResult);
            System.out.println("============ Finish batching " + (i + 1) + " ============ ");
        }
        totalResult = totalResult.avg(10);
        for (int i = 1; i < totalResult.topAccuracy.size(); i++) {
            totalResult.topAccuracy.set(i, totalResult.topAccuracy.get(i) + totalResult.topAccuracy.get(i - 1));
        }
        System.out.println(totalResult);
    }

    private BenchResult batch(List<GTTreeEdit> train, List<GTTreeEdit> test) {
        GTHierarchicalCalculator cc = new GTHierarchicalCalculator(new PlainAntiUnifier2());
        long TRAINING_START = System.currentTimeMillis();
        List<Cluster<GTTreeEdit>> clusters = cc.cluster(train);
        long TRAINING_END = System.currentTimeMillis();
        ClusterManager cm = new ClusterManager(Matchers.getInstance().getMatcher());
        cm.initClusters(clusters);

        int examples = train.size() + test.size();
        long predictionTime = 0;
        List<Integer> accurate = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (var treeEdit : test) {
            long PREDICTION_START = System.currentTimeMillis();
            List<MatchResult> matchResults = cm.ranking(treeEdit.before());
            long PREDICTION_END = System.currentTimeMillis();
            predictionTime += PREDICTION_END - PREDICTION_START;

            Tree expected = treeEdit.after();
            while (accurate.size() < matchResults.size()) accurate.add(0);
            while (counts.size() < matchResults.size()) counts.add(0);
            for (int i = 0 ; i < matchResults.size(); i++) {
                var originCount = counts.get(i);
                counts.set(i, originCount + 1);
                Tree actual = matchResults.get(i).after();
                if (GTTreeUtils.treeEquals(expected, actual)) {
                    var origin = accurate.get(i);
                    accurate.set(i, origin + 1);
                }
            }
        }

        List<Double> topAccuracy = new ArrayList<>();
        for (int i = 0; i < accurate.size(); i++) {
            topAccuracy.add((double) accurate.get(i) / counts.get(i));
        }
        return new BenchResult(
                examples,
                TRAINING_END - TRAINING_START,
                predictionTime,
                (double) predictionTime / test.size(),
                topAccuracy
        );
    }

    private record BenchResult(
            int examples,
            long trainingTime,
            long predictionTime,
            double singlePrediction,
            List<Double> topAccuracy
    ) {
        public BenchResult plus(BenchResult rhs) {
            List<Double> newTopAccuracy = new ArrayList<>();
            for (int i = 0; i < Math.max(this.topAccuracy.size(), rhs.topAccuracy.size()); i++) {
                var v1 = i < this.topAccuracy.size() ? this.topAccuracy.get(i) : 0;
                var v2 = i < rhs.topAccuracy.size() ? rhs.topAccuracy.get(i) : 0;
                newTopAccuracy.add(v1 + v2);
            }

            return new BenchResult(
                    examples + rhs.examples,
                    trainingTime + rhs.trainingTime,
                    predictionTime + rhs.predictionTime,
                    singlePrediction + rhs.singlePrediction,
                    newTopAccuracy
            );
        }

        public BenchResult avg(int count) {
            return new BenchResult(
                    examples / count,
                    trainingTime / count,
                    predictionTime / count,
                    singlePrediction / count,
                    new ArrayList<>(topAccuracy.stream().map(i -> i / count).toList())
            );
        }

        @Override
        public String toString() {
            return "BenchResult{" +
                    "examples=" + examples +
                    ", trainingTime=" + trainingTime +
                    ", predictionTime=" + predictionTime +
                    ", singlePrediction=" + singlePrediction +
                    ", topAccuracy=" + topAccuracy +
                    '}';
        }
    }
}
