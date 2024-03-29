package com.cpunisher.hasakafix.main;

import com.cpunisher.hasakafix.bean.EditFile;
import com.cpunisher.hasakafix.git.GitDiffFileRecord;
import com.cpunisher.hasakafix.git.GitHelper;
import com.cpunisher.hasakafix.utils.IdentityPair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@CommandLine.Command(name = "edit-files", description = "Extract git commits which are probably used to fix bugs")
public class CommandEditFiles implements Runnable {
    @CommandLine.Parameters(paramLabel = "<repos>")
    List<File> repos = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--output-dir"}, defaultValue = "./output")
    File outputDir;

    private boolean isTest(String filename) {
        return filename.contains("Test") || filename.contains("test");
    }

    private boolean commitMessagePredicate(RevCommit commit) {
        String[] includes = new String[]{"fix", "bug fix"};
        String[] excludes = new String[]{"fix typo", "fix build", "non-fix"};
        String message = commit.getFullMessage();
        return Arrays.stream(includes).anyMatch(message::contains) && Arrays.stream(excludes).noneMatch(message::contains);
    }

    private boolean commitContainTestPredicate(List<GitDiffFileRecord> records) {
        boolean source = false;
        boolean test = false;
        for (GitDiffFileRecord record : records) {
            if (isTest(record.before().filepath()) || isTest(record.after().filepath())) {
                test = true;
            } else {
                source = true;
            }
        }
        return test && source;
    }

    private boolean commitChangedLineLimitPredicate(List<GitDiffFileRecord> records) {
        int sum = records.stream()
                .filter(record -> !isTest(record.before().filepath()) && !isTest(record.after().filepath()))
                .map(record -> record.changedLineCountA() + record.changedLineCountB())
                .reduce(0, Integer::sum);
        return sum > 0 && sum <= 4;
    }

    @Override
    public void run() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (File repo : repos) {
            File repoGit = repo.toPath().resolve(".git").toFile();
            try {
                GitHelper gitHelper = new GitHelper(repoGit, ".java");
                // IdentityPair<EditFile> -> each changed file in a commit
                // List<IdentityPair<EditFile>> -> changed files in a commit
                Map<String, List<IdentityPair<EditFile>>> editFiles = StreamSupport.stream(gitHelper.getCommits().spliterator(), true)
                        .filter(this::commitMessagePredicate)
                        .map(gitHelper::getEditFiles)
                        .filter(this::commitContainTestPredicate)
                        .filter(this::commitChangedLineLimitPredicate)
                        .flatMap(records -> records.stream()
                                .filter(record -> !isTest(record.before().filepath()) && !isTest(record.after().filepath()))
                        )
                        .collect(Collectors.groupingBy(
                                gitDiffFileRecord -> gitDiffFileRecord.after().commit().getName(),
                                Collectors.mapping(diff -> diff.toEditFiles(gitHelper), Collectors.toList()))
                        );

                int total = editFiles.size(), finish = 0;
                Files.createDirectories(outputDir.toPath());
                System.out.printf("Find %d commits\n", total);
                for (var entry : editFiles.entrySet()) {
                    Files.writeString(outputDir.toPath().resolve(entry.getKey() + ".json"), gson.toJson(entry.getValue()));
                    System.out.printf("[%d/%d] Finish commit %s\n", ++finish, total, entry.getKey());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
