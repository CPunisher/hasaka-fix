package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.bean.EditFile;
import com.cpunisher.hasakafix.edit.editor.IEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTEditor;
import com.cpunisher.hasakafix.edit.editor.gumtree.GTTreeEdit;
import com.cpunisher.hasakafix.edit.parser.GTSourceParser;
import com.cpunisher.hasakafix.edit.parser.ISourceParser;
import com.cpunisher.hasakafix.utils.IdentityPair;
import com.cpunisher.hasakafix.utils.PathUtil;
import com.cpunisher.hasakafix.utils.XmlHelper;
import com.github.gumtreediff.io.TreeIoUtils;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

@CommandLine.Command(name = "concrete-edits", description = "Get concrete edits of changes of git commits")
public class CommandConcreteEdits implements Runnable {
    @CommandLine.Parameters(paramLabel = "<edit-file>")
    List<File> editFiles = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--output-dir"}, defaultValue = "./output")
    File outputDir;

    @Override
    public void run() {
        ISourceParser<Tree> parser = new GTSourceParser(".java");
        IEditor<Tree, GTTreeEdit> editor = new GTEditor();

        Gson gson = new Gson();
        Type jsonType = new TypeToken<List<IdentityPair<EditFile>>>() {
        }.getType();
        int total = editFiles.size(), finish = 0;
        for (File file : editFiles) {
            List<IdentityPair<EditFile>> editFile;
            try {
                String raw = Files.readString(file.toPath());
                editFile = gson.fromJson(raw, jsonType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<IdentityPair<TreeIoUtils.TreeSerializer>> serializers = new ArrayList<>();
            for (var pair : editFile) {
                Tree before = parser.parse(pair.first.content());
                Tree after = parser.parse(pair.second.content());
                Set<IdentityPair<TreeIoUtils.TreeSerializer>> edits = editor.getEdits(before, after)
                        .stream()
                        .map(edit -> new IdentityPair<>(
                                TreeIoUtils.toXml(XmlHelper.toTreeContext(edit.before())),
                                TreeIoUtils.toXml(XmlHelper.toTreeContext(edit.after()))
                        )).collect(Collectors.toSet());
                serializers.addAll(edits);
            }

            try {
                for (int i = 0; i < serializers.size(); i++) {
                    var pair = serializers.get(i);
                    Path dir = PathUtil.removeExtension(outputDir.toPath().resolve(file.getName()));
                    Files.createDirectories(dir);
                    pair.first.writeTo(dir.resolve("before_" + i + ".xml").toFile());
                    pair.second.writeTo(dir.resolve("after_" + i + ".xml").toFile());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.printf("[%d/%d] Finish file %s\n", ++finish, total, file.getName());
        }
    }
}
