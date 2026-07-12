package dev.chpg.pg.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class GlobalIdGeneratorTest {

    @Test
    public void testConcurrentIdGeneration() throws InterruptedException {
        int threadCount = 10;
        int idsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<Integer> generatedNodeIds = ConcurrentHashMap.newKeySet();
        Set<Integer> generatedEdgeIds = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < idsPerThread; j++) {
                        generatedNodeIds.add(GlobalIdGenerator.INSTANCE.createNodeId());
                        generatedEdgeIds.add(GlobalIdGenerator.INSTANCE.createEdgeId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));

        assertEquals(threadCount * idsPerThread, generatedNodeIds.size(), "All generated node IDs must be unique");
        for (int id : generatedNodeIds) {
            assertTrue(id > 0, "Node IDs must be strictly positive");
        }

        assertEquals(threadCount * idsPerThread, generatedEdgeIds.size(), "All generated edge IDs must be unique");
        for (int id : generatedEdgeIds) {
            assertTrue(id > 0, "Edge IDs must be strictly positive");
        }
    }
}
