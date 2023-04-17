package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.antiunification.PlainAntiUnifier2;
import com.cpunisher.hasakafix.apply.ClusterManager;
import com.cpunisher.hasakafix.apply.MatchResult;
import com.cpunisher.hasakafix.bean.Cluster;
import com.cpunisher.hasakafix.cluster.GTHierarchicalCalculator;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.utils.tree.GTTreeUtils;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@CommandLine.Command(name = "bench", description = "10-fold cross validation")
public class CommandBench implements Runnable {
    @CommandLine.Parameters(paramLabel = "<concrete-edits>")
    List<File> concreteEdits = new ArrayList<>();

    enum GroupStrategy {MANUAL, MIXED, MIXED_SHUFFLE}

    ;
    @CommandLine.Option(names = {"-gs", "--group-strategy"})
    GroupStrategy strategy = GroupStrategy.MIXED_SHUFFLE;

    @Override
    public void run() {
        if (!concreteEdits.stream().allMatch(dir -> dir.exists() && dir.isDirectory())) {
            System.out.println("Only directory is allowed!");
            return;
        }
        switch (strategy) {
            case MANUAL -> runManual();
            case MIXED -> runMixed();
            case MIXED_SHUFFLE -> runMixedShuffle();
        }
    }

    private void runManual() {
        System.out.println("[Mode: Manual] Loading files...");
        List<List<GTTreeEdit>> editGroups = new ArrayList<>();
        for (var group : concreteEdits) {
            List<GTTreeEdit> edits = new ArrayList<>();
            for (var commit : Objects.requireNonNull(group.listFiles())) {
                edits.addAll(GTTreeUtils.readCommitDir(commit));
            }
            editGroups.add(edits);
        }

        System.out.println("[Mode: Manual] 10-fold cross validation");
        BenchResult totalResult = tenFoldCrossValidate(editGroups.subList(0, 10));
        System.out.println(totalResult);
    }

    private void runMixed() {
        System.out.println("[Mode: MIXED] Loading files...");
        List<GTTreeEdit> mixedEdits = new ArrayList<>();
        for (var commit : concreteEdits) {
            mixedEdits.addAll(GTTreeUtils.readCommitDir(commit));
        }

        System.out.println("[Mode: MIXED] 10-fold cross validation");
        List<List<GTTreeEdit>> editGroups = Lists.partition(mixedEdits, mixedEdits.size() / 10).subList(0, 10);
        BenchResult totalResult = tenFoldCrossValidate(editGroups);
        System.out.println(totalResult);
    }

    private void runMixedShuffle() {
        System.out.println("[Mode: MIXED_SHUFFLE] Loading files...");
        List<GTTreeEdit> mixedEdits = new ArrayList<>();
        for (var commit : concreteEdits) {
            mixedEdits.addAll(GTTreeUtils.readCommitDir(commit));
        }

        System.out.println("[Mode: MIXED_SHUFFLE] 10-fold cross validation");
        Collections.shuffle(mixedEdits);
        List<List<GTTreeEdit>> editGroups = Lists.partition(mixedEdits, mixedEdits.size() / 10).subList(0, 10);
        BenchResult totalResult = tenFoldCrossValidate(editGroups);
        System.out.println(totalResult);
    }

    private BenchResult tenFoldCrossValidate(List<List<GTTreeEdit>> editGroups) {
        if (editGroups.size() != 10) {
            throw new IllegalArgumentException("Size of edit groups must be 10, actual: " + editGroups.size());
        }
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
        return totalResult;
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

        for (var treeEdit : test) {
            long PREDICTION_START = System.currentTimeMillis();
            List<MatchResult> matchResults = cm.ranking(treeEdit.before());
            long PREDICTION_END = System.currentTimeMillis();
            predictionTime += PREDICTION_END - PREDICTION_START;

            Tree expected = treeEdit.after();
            while (accurate.size() < matchResults.size()) accurate.add(0);
            for (int i = 0; i < matchResults.size(); i++) {
                Tree actual = matchResults.get(i).after();
                if (GTTreeUtils.treeEquals(expected, actual)) {
                    var origin = accurate.get(i);
                    accurate.set(i, origin + 1);
                }
            }
        }

        List<Double> topAccuracy = new ArrayList<>();
        for (int i = 0; i < accurate.size(); i++) {
            topAccuracy.add((double) accurate.get(i) / test.size());
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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(this);
        }
    }
}
