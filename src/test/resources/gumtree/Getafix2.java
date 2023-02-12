public class Worker {
    private long getRuntime() {
        return now - start;
    }
    public void doWorker() {
        if (task == null)
            return;
        task.makeProgress();
    }
}
