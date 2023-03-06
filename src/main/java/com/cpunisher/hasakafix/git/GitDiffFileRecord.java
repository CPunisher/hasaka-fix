package com.cpunisher.hasakafix.git;

import com.cpunisher.hasakafix.bean.EditFile;
import com.cpunisher.hasakafix.utils.IdentityPair;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public record GitDiffFileRecord(
        GitDiffFile before,
        GitDiffFile after,
        List<Edit> edits
) {

    public int changedLineCount() {
        return edits.stream().map(Edit::getLengthA).reduce(0, Integer::sum);
    }

    public IdentityPair<EditFile> toEditFiles(GitHelper gitHelper) {
        return new IdentityPair<>(
                new EditFile(gitHelper.getFileContent(before().commit(), before().filepath()), before().filepath()),
                new EditFile(gitHelper.getFileContent(after().commit(), after().filepath()), after().filepath())
        );
    }

    public record GitDiffFile(
            RevCommit commit,
            String filepath
    ) {
    }
}
