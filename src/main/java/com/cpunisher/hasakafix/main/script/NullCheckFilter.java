package com.cpunisher.hasakafix.main.script;

import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.tree.TypeSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

public class NullCheckFilter {
    static final String CE_ROOT_DIR = "./output/ce/";
    static final String OUT_DIR = "./output/ce/null_ce/";
    static final Matcher matcher = Matchers.getInstance().getMatcher();

    public static void main(String[] args) throws IOException {
        List<Path> ceDirs = Files.list(Path.of(CE_ROOT_DIR))
                .filter(path -> !path.toString().contains("vgroup"))
                .toList();

        Map<String, List<Integer>> result = new HashMap<>();
        for (Path ceDir : ceDirs) {
//            System.out.println(ceDir);
            List<Path> commitDirs = Files.list(ceDir).toList();
            for (Path commitDir : commitDirs) {
                int count = Objects.requireNonNull(commitDir.toFile().list()).length / 2;
                for (int i = 0; i < count; i++) {
                    Path before = commitDir.resolve("before_" + i + ".xml");
                    Path after = commitDir.resolve("after_" + i + ".xml");
                    Tree beforeTree = TreeIoUtils.fromXml().generateFrom().file(before).getRoot();
                    Tree afterTree = TreeIoUtils.fromXml().generateFrom().file(after).getRoot();
                    var mappings = matcher.match(beforeTree, afterTree);
                    EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
                    EditScript editScript = editScriptGenerator.computeActions(mappings);

                    boolean flag = StreamSupport.stream(editScript.spliterator(), false)
                            .flatMap(action -> StreamSupport.stream(action.getNode().preOrder().spliterator(), false))
                            .anyMatch(node -> node.getType().equals(TypeSet.type("NullLiteral")));
                    if (flag) {
                        result.computeIfAbsent(commitDir.toString(), k -> new ArrayList<>())
                                .add(i);
                    }
                }
            }
        }
//        result.forEach((key, value) -> System.out.println(key + ":" + value.size()));
        for (var entry : result.entrySet()) {
            Path commitDir = Path.of(entry.getKey());
            System.out.println(entry.getKey());
            for (int i = 0; i < entry.getValue().size(); i++) {
                Path before = commitDir.resolve("before_" + entry.getValue().get(i) + ".xml");
                Path after = commitDir.resolve("after_" + entry.getValue().get(i) + ".xml");
                Path beforeOut = Path.of(OUT_DIR).resolve(commitDir.getFileName()).resolve("before_" + i + ".xml");
                Path afterOut = Path.of(OUT_DIR).resolve(commitDir.getFileName()).resolve("after_" + i + ".xml");
                if (!beforeOut.getParent().toFile().exists()) {
                    Files.createDirectories(beforeOut.getParent());
                }

                if (!beforeOut.toFile().exists() && !afterOut.toFile().exists()) {
                    Files.copy(before, beforeOut);
                    Files.copy(after, afterOut);
                }
            }
        }
    }
}
