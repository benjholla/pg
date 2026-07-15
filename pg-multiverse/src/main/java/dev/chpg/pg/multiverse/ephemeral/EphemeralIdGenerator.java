package dev.chpg.pg.multiverse.ephemeral;

import java.util.concurrent.atomic.AtomicInteger;

/** Generates IDs for the Ephemeral sandbox. */
/** Generates IDs for the Ephemeral sandbox. */
public final class EphemeralIdGenerator {

    // reserving zero and positive values for multiverse elements
    private AtomicInteger nextNodeId = new AtomicInteger(-1);
    private AtomicInteger nextEdgeId = new AtomicInteger(-1);

    /**
     * Creates a new node id.
     * @return the node id
     */
    /**
     * Creates a new node id.
     * @return the node id
     */
    public int createNodeId() {
        return nextNodeId.getAndDecrement();
    }

    /**
     * Creates a new edge id.
     * @return the edge id
     */
    /**
     * Creates a new edge id.
     * @return the edge id
     */
    public int createEdgeId() {
        return nextEdgeId.getAndDecrement();
    }
}
