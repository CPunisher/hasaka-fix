public class Worker {
    public long getRuntime() {
        return now - start;
    }
    public void doWorker() {
        task.makeProgress();
    }
}