import subprocess

out_dir = "./output/bench/bench_group"
ce_dir = "./output/ce/group"
bench_cmd = "java -jar build/libs/hasaka-fix-1.0-SNAPSHOT.jar bench -gs MIXED_SHUFFLE "

for count in range(1, 21):
    print("Start group " + str(count))
    f = open(out_dir + str(count), "w")
    cmd = bench_cmd + ' '.join([ce_dir + str(i) + '/*' for i in range(1, count + 1)])
    subprocess.call(["sh", "-c", cmd], stdout=f)