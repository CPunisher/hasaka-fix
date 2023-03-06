package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.antiunification.GTAntiUnifier;
import com.cpunisher.hasakafix.bean.EditFile;
import com.cpunisher.hasakafix.edit.editor.IEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.cpunisher.hasakafix.utils.IdentityPair;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

@CommandLine.Command(name = "concrete-edits")
public class CommandConcreteEdits implements Runnable {
    @CommandLine.Parameters(paramLabel = "<edit-file>")
    List<File> editFiles = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--output-dir"}, defaultValue = "./output")
    File outputDir;

    @Override
    public void run() {
        ISourceParser<Tree> parser = new GTSourceParser(".java");
        IEditor<Tree, GTTreeEdit> editor = new GTEditor();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type jsonType = new TypeToken<List<IdentityPair<EditFile>>>() {
        }.getType();
        for (File file : editFiles) {
            List<IdentityPair<EditFile>> editFile;
            try {
                String raw = Files.readString(file.toPath());
                editFile = gson.fromJson(raw, jsonType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<Set<IdentityPair<String>>> result = new ArrayList<>();
            for (var pair : editFile) {
                Tree before = parser.parse(pair.first.content());
                Tree after = parser.parse(pair.second.content());
                Set<IdentityPair<String>> edits = editor.getEdits(before, after)
                        .stream()
                        .map(edit -> new IdentityPair<>(GTAntiUnifier.treeToString(edit.before()), GTAntiUnifier.treeToString(edit.after())))
                        .collect(Collectors.toSet());
                result.add(edits);
            }

            try {
                Path target = outputDir.toPath().resolve(file.getName());
                Files.createDirectories(target.getParent());
                Files.writeString(target, gson.toJson(result));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
