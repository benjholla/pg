package dev.chpg.pg.global;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * undocumented.
 */
public enum GlobalIdGenerator {
    /**
     * undocumented.
     */
    INSTANCE;

    // reserving negative values and zero for future capabilities
    private AtomicInteger nextNodeId = new AtomicInteger(1);
    private AtomicInteger nextEdgeId = new AtomicInteger(1);

    /**
     * undocumented.
     */
    public int createNodeId() {
        return nextNodeId.getAndIncrement();
    }

    /**
     * undocumented.
     */
    public int createEdgeId() {
        return nextEdgeId.getAndIncrement();
    }
}
