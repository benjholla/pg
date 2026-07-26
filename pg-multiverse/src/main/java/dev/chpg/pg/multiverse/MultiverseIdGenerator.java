package dev.chpg.pg.multiverse;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique IDs for universes.
 *
 * This is a singleton enum providing thread-safe, monotonically increasing
 * universe IDs via an internal AtomicInteger.
 */
public enum MultiverseIdGenerator {
    /**
     * The singleton instance of the generator.
     */
    INSTANCE;

    // reserving negative values and zero for future capabilities
    private final AtomicInteger nextUniverseId = new AtomicInteger(1);

    /**
     * Creates and returns a new globally unique universe ID.
     *
     * @return a unique integer universe ID
     */
    public int createUniverseId() {
        return nextUniverseId.getAndIncrement();
    }
}
