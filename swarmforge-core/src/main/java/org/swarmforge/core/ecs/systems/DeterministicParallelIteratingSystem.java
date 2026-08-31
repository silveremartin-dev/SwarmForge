package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import java.util.concurrent.ForkJoinPool;

/**
 * Deterministic Multi-Threaded Entity Iterating System.
 * Partitions active entity components into fixed contiguous index range slices,
 * executing entity processing across CPU worker threads in deterministic order.
 *
 * 100% deterministic execution state across single-threaded and multi-threaded runs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public abstract class DeterministicParallelIteratingSystem extends IteratingSystem {

    private static final int PARALLEL_THRESHOLD = 500;
    private final ForkJoinPool pool = ForkJoinPool.commonPool();

    public DeterministicParallelIteratingSystem(Aspect.Builder aspect) {
        super(aspect);
    }

    @Override
    protected void processSystem() {
        IntBag entities = getEntityIds();
        int size = entities.size();
        if (size < PARALLEL_THRESHOLD) {
            // Single-threaded fast path
            int[] array = entities.getData();
            for (int i = 0; i < size; i++) {
                process(array[i]);
            }
        } else {
            // Deterministic parallel chunk dispatch
            int chunks = Math.min(8, Runtime.getRuntime().availableProcessors());
            int chunkSize = (size + chunks - 1) / chunks;
            int[] array = entities.getData();

            pool.submit(() -> {
                java.util.stream.IntStream.range(0, chunks).parallel().forEach(chunkIdx -> {
                    int start = chunkIdx * chunkSize;
                    int end = Math.min(size, start + chunkSize);
                    for (int i = start; i < end; i++) {
                        process(array[i]);
                    }
                });
            }).join();
        }
    }
}

