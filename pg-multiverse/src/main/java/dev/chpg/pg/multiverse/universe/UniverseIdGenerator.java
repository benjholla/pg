package dev.chpg.pg.multiverse.universe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Generates IDs for the Universe graph. */
public final class UniverseIdGenerator {

    private final AtomicInteger nextNodeId = new AtomicInteger(1);
    private final AtomicInteger nextEdgeId = new AtomicInteger(1);
    private final AtomicLong universeModCount = new AtomicLong(0);

    /**
     * Creates a new node id.
     * @return the node id
     */
    public int createNodeId() {
        return nextNodeId.getAndIncrement();
    }

    /**
     * Creates a new edge id.
     * @return the edge id
     */
    public int createEdgeId() {
        return nextEdgeId.getAndIncrement();
    }

    /**
     * Returns the high-water mark of allocated node IDs.
     * @return the number of allocated node IDs
     */
    public int allocatedNodeCount() {
        return nextNodeId.get();
    }

    /**
     * Returns the high-water mark of allocated edge IDs.
     * @return the number of allocated edge IDs
     */
    public int allocatedEdgeCount() {
        return nextEdgeId.get();
    }

    /**
     * Increments the universe modification count and returns the updated value.
     * @return the updated modification count
     */
    public long incrementAndGetModCount() {
        return universeModCount.incrementAndGet();
    }

    /**
     * Gets the current universe modification count.
     * @return the modification count
     */
    public long getModCount() {
        return universeModCount.get();
    }
}
