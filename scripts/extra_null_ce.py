import os
import shutil

ce_root_dir = "./output/ce/"
out_dir = "./output/ce/null_ce/"


def listdir_join(target):
    return [os.path.join(target, file) for file in os.listdir(target)]


ce_dirs = list(filter(lambda p: "vgroup" not in p, listdir_join(ce_root_dir)))
null_check_result = []
for ce_dir in ce_dirs:
    print(ce_dir)
    commit_dirs = listdir_join(ce_dir)
    for commit_dir in commit_dirs:
        size = len(listdir_join(commit_dir))
        for i in range(size // 2):
            before_file = os.path.join(commit_dir, f"before_{i}.xml")
            after_file = os.path.join(commit_dir, f"after_{i}.xml")
            with open(before_file) as fp_before, open(after_file) as fp_after:
                content_before = fp_before.read()
                content_after = fp_after.read()
                if "NullLiteral" in content_before or "NullLiteral" in content_after:
                    null_check_result.append([before_file, after_file])

for [before_file, after_file] in null_check_result:
    parent_dir = os.path.dirname(os.path.dirname(before_file))
    out_before_file = os.path.join(out_dir, os.path.relpath(before_file, parent_dir))
    out_after_file = os.path.join(out_dir, os.path.relpath(after_file, parent_dir))
    os.makedirs(os.path.dirname(out_before_file), exist_ok=True)
    shutil.copy(before_file, out_before_file)
    shutil.copy(after_file, out_after_file)
