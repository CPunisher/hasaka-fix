package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.bean.EditFile;
import com.cpunisher.hasakafix.utils.IdentityPair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GitHelper {

    private final Repository repository;
    private final String extension;

    public GitHelper(File gitDir, String extension) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        this.repository = builder.setGitDir(gitDir)
                .readEnvironment()
                .findGitDir()
                .build();
        this.extension = extension;
    }

    public List<IdentityPair<EditFile>> getEditFiles(RevCommit commit) {
        if (commit.getParentCount() != 1) {
            return Collections.emptyList();
        }
        RevCommit prev = commit.getParent(0);

        try (
                Git git = new Git(repository);
                ObjectReader reader = repository.newObjectReader()
        ) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, prev.getTree());
            newTreeIter.reset(reader, commit.getTree());
            List<DiffEntry> diffs = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();
            List<IdentityPair<EditFile>> modifiedFiles = new ArrayList<>();
            for (DiffEntry diffEntry : diffs) {
                if (diffEntry.getChangeType() != DiffEntry.ChangeType.MODIFY) {
                    continue;
                }
                String oldPath = diffEntry.getOldPath();
                String newPath = diffEntry.getNewPath();
                if (newPath.endsWith(extension)) {
                    modifiedFiles.add(new IdentityPair<>(
                            new EditFile(getFileContent(prev, oldPath), oldPath),
                            new EditFile(getFileContent(commit, newPath), newPath)
                    ));
                }
            }
            return modifiedFiles;
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public String getFileContent(RevCommit commit, String filePath) {
        RevTree revTree = commit.getTree();
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(revTree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));
            if (!treeWalk.next()) {
                throw new IllegalStateException("Did not find expected file " + filePath);
            }

            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);
            return new String(loader.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Iterable<RevCommit> getCommits() {
        try (Git git = new Git(repository)) {
            return git.log().call();
        } catch (GitAPIException exception) {
            exception.printStackTrace();
        }
        return Collections.emptyList();
    }
}
