package io.github.benjholla.pg.universe.ephemeral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class EphemeralIdGeneratorTest {
    private static final EphemeralFactory factory = new EphemeralGraph().factory();


    @Test
    public void testConcurrentIdGeneration() throws InterruptedException {
        int threadCount = 10;
        int idsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Integer> generatedIds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        generatedIds.add(EphemeralIdGenerator.INSTANCE.create());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));

        assertEquals(threadCount * idsPerThread, generatedIds.size(), "All generated IDs must be unique");
        for (int id : generatedIds) {
            assertTrue(id < 0, "IDs must be strictly negative");
        }
    }
}
