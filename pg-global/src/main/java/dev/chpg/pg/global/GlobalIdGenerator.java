package dev.chpg.pg.global;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A singleton generator for issuing globally unique IDs for the {@code pg-global} graph implementation.
 * <p>
 * <b>What it represents:</b> A thread-safe, monotonic counter providing unique primitive identifiers.
 * <p>
 * <b>Why it exists:</b> To ensure that nodes and edges created across different instances of {@link GlobalGraph} do not collide, enabling safe topology comparisons and algebraic operations.
 * <p>
 * <b>Thread safety:</b> This singleton is thread-safe and utilizes atomic integers to prevent race conditions during concurrent ID creation.
 */
public enum GlobalIdGenerator {
    /**
     * The singleton instance of the generator.
     */
    INSTANCE;

    // reserving negative values and zero for future capabilities
    private AtomicInteger nextNodeId = new AtomicInteger(1);
    private AtomicInteger nextEdgeId = new AtomicInteger(1);

    /**
     * Creates and returns a new globally unique node ID.
     *
     * @return a unique integer node ID
     */
    public int createNodeId() {
        return nextNodeId.getAndIncrement();
    }

    /**
     * Creates and returns a new globally unique edge ID.
     *
     * @return a unique integer edge ID
     */
    public int createEdgeId() {
        return nextEdgeId.getAndIncrement();
    }
}
