package dev.chpg.pg.global;

import java.util.concurrent.atomic.AtomicInteger;

public enum GlobalIdGenerator {
    INSTANCE;

    // reserving negative values and zero for future capabilities
    private AtomicInteger nextNodeId = new AtomicInteger(1);
    private AtomicInteger nextEdgeId = new AtomicInteger(1);

    public int createNodeId() {
        return nextNodeId.getAndIncrement();
    }

    public int createEdgeId() {
        return nextEdgeId.getAndIncrement();
    }
}
