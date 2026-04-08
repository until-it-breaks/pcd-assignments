package pcd.poool.model.collision;

import pcd.poool.model.ball.Ball;
import pcd.poool.model.ball.Balls;

import java.util.ArrayList;
import java.util.List;

/**
 * A multithreaded {@link CollisionResolver} that manages raw {@link Thread} lifecycles manually.
 * <p>
 * This resolver uses a mirrored strided distribution strategy to balance
 * the O(n²) workload across a fixed number of threads.
 * By using a stride to interleave
 * row assignments and pairing each row with its mirror ({@code j} and {@code n-1-j}),
 * it folds the triangular workload in an attempt to equalize the computational
 * effort across all threads.
 * </p>
 * <p>
 * <b>Workload Distribution Example (5 balls, 2 threads):</b>
 * <pre>
 * Ball pairs to check:
 * Row 0: (0,1) (0,2) (0,3) (0,4) [4 checks]
 * Row 1:       (1,2) (1,3) (1,4) [3 checks]
 * Row 2:             (2,3) (2,4) [2 checks]
 * Row 3:                   (3,4) [1 check ]
 * Row 4:                         [0 checks]
 *
 * Distribution:
 * Thread 0: Row 0 + Row 4 + Row 2 = 6 checks
 * Thread 1: Row 1 + Row 3 = 4 checks
 * </pre>
 * </p>
 */
public class ThreadedCollisionResolver implements CollisionResolver {
    private final int threadCount;

    public ThreadedCollisionResolver() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public ThreadedCollisionResolver(int threadCount) {
        this.threadCount = threadCount;
    }

    public void resolve(List<Ball> balls) throws InterruptedException {
        int n = balls.size();
        List<Thread> threads = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads.add(new Thread(() -> {
                for (int j = threadIndex; j < n / 2; j += threadCount) {
                    processRow(j, balls);           // Frontmost available row
                    processRow(n - 1 - j, balls);   // Backmost available row
                }
                // Handle middle row for odd counts. It gets assigned to the first thread.
                if (n % 2 != 0 && threadIndex == 0) {
                    processRow(n / 2, balls);
                }
            }));
            threads.get(i).start();
        }
        for (Thread t : threads) t.join();
    }

    protected void processRow(int i, List<Ball> balls) {
        Ball ball = balls.get(i);
        for (int j = i + 1; j < balls.size(); j++) {
            Balls.resolveCollisionSynchronized(ball, balls.get(j));
        }
    }
}
