package pcd.poool.util;

public class LatchImplementation implements Latch {
    private int count;

    public LatchImplementation(int count) {
        this.count = count;
    }

    @Override
    public synchronized void countDown() {
        if (count > 0) {
            count--;
            if (count == 0) {
                notifyAll();
            }
        }
    }

    @Override
    public synchronized void await() throws InterruptedException {
        while (count > 0) {
            wait();
        }
    }
}
