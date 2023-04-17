import math
import json
import os
import glob
import subprocess


def get_stats_of_group(group):
    repo_root = "./dataset/group" + str(group)
    repos = os.listdir(repo_root)
    repo_size = len(repos)
    repo_commit_size = 0
    for repo in repos:
        try:
            output = subprocess.check_output(
                ['git', '-C', os.path.join(repo_root, repo), 'rev-list', 'HEAD', '--count'])
            repo_commit_size += int(output)
        except Exception:
            pass

    commit_root = "./output/edit/group" + str(group)
    filtered_commits = os.listdir(commit_root)
    filtered_commit_size = len(filtered_commits)
    filtered_commit_code_lines = 0
    for commit_file in filtered_commits:
        commit = json.load(open(os.path.join(commit_root, commit_file)))
        for modified_file in commit:
            filtered_commit_code_lines += len(modified_file['second']['content'].split('\n'))

    edit_size = math.floor(
        sum(1 for _ in glob.iglob("./output/ce/group" + str(group) + "/**/*.xml", recursive=True)) / 2
    )
    return [repo_size, repo_commit_size, filtered_commit_size, filtered_commit_code_lines, edit_size]


group_count = 20
last = [0] * 5
for i in range(1, group_count + 1):
    result = get_stats_of_group(i)
    if i > 0:
        result = [result[j] + last[j] for j in range(0, 5)]
    last = result
    print(result)
