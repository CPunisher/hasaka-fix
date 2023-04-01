package com.cpunisher.hasakafix;

import com.cpunisher.hasakafix.bean.EditFile;
import com.cpunisher.hasakafix.git.GitDiffFileRecord;
import com.cpunisher.hasakafix.git.GitHelper;
import com.cpunisher.hasakafix.utils.IdentityPair;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.cpunisher.hasakafix.repo.Simple1.NEW_WORKER_DOT_JAVA;
import static com.cpunisher.hasakafix.repo.Simple1.OLD_WORKER_DOT_JAVA;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.StreamSupport;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GitHelperTest {

    private GitHelper gitHelper;

    @BeforeAll
    public void initHelper() throws IOException, URISyntaxException {
        URI gitUri = GitHelperTest.class
                .getClassLoader()
                .getResource("repo/simple1/.git")
                .toURI();
        gitHelper = new GitHelper(new File(gitUri), ".java");
    }

    @Test
    public void testGetCommits() {
        long count = StreamSupport.stream(gitHelper.getCommits().spliterator(), false)
                .count();
        assertEquals(2, count);
    }

    @Test
    public void testGetFileContent() {
        RevCommit commit = gitHelper.getCommits().iterator().next();
        String filePath = "Worker.java";
        String content = gitHelper.getFileContent(commit, filePath);
        assertEquals(NEW_WORKER_DOT_JAVA, content);
    }

    @Test
    public void testGetEditFiles() {
        RevCommit commit = gitHelper.getCommits().iterator().next();

        List<IdentityPair<EditFile>> files = gitHelper.getEditFiles(commit)
                .stream()
                .map(record -> record.toEditFiles(gitHelper))
                .toList();
        assertEquals(1, files.size());

        IdentityPair<EditFile> pair = files.get(0);
        EditFile oldFile = pair.first;
        EditFile newFile = pair.second;
        assertEquals("Worker.java", oldFile.filepath());
        assertEquals("Worker.java", newFile.filepath());
        assertEquals(OLD_WORKER_DOT_JAVA, oldFile.content());
        assertEquals(NEW_WORKER_DOT_JAVA, newFile.content());
    }

    @Test
    public void testChangedLines() {
        RevCommit commit = gitHelper.getCommits().iterator().next();
        List<GitDiffFileRecord> list = gitHelper.getEditFiles(commit);
        assertEquals(1, list.get(0).changedLineCountA());
        assertEquals(4, list.get(0).changedLineCountB());
    }
}