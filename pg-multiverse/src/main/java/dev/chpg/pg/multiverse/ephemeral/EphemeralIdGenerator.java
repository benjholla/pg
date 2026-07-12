package dev.chpg.pg.multiverse.ephemeral;

import java.util.concurrent.atomic.AtomicInteger;

public final class EphemeralIdGenerator {

    // reserving zero and positive values for multiverse elements
    private AtomicInteger nextNodeId = new AtomicInteger(-1);
    private AtomicInteger nextEdgeId = new AtomicInteger(-1);

    public int createNodeId() {
        return nextNodeId.getAndDecrement();
    }

    public int createEdgeId() {
        return nextEdgeId.getAndDecrement();
    }
}
