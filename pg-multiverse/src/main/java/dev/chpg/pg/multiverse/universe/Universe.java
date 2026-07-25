package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.Graph;
import dev.chpg.pg.multiverse.ephemeral.EphemeralGraph;
import java.util.Objects;

/**
 * The Central Registry and stateful engine for a pg-multiverse instance.
 *
 * Phase 1 Shell: Establishes identity, concurrency boundaries, and
 * prepares the architectural footprint for Ephemeral promotion.
 */
public class Universe {

    private final UniverseIdGenerator idGenerator;

    /**
     * Instantiates a completely isolated Universe with its own ID space and modCount.
     */
    public Universe() {
        this.idGenerator = new UniverseIdGenerator();
    }

    /**
     * Optional constructor if you need to inject a specifically configured generator.
     *
     * @param idGenerator the ID generator to inject
     */
    public Universe(UniverseIdGenerator idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "IdGenerator cannot be null");
    }

    // =========================================================================
    // 1. IDENTITY & CONCURRENCY DELEGATION
    // =========================================================================

    /**
     * Exposes the isolated ID generator for this specific Universe instance.
     *
     * @return the isolated ID generator
     */
    public UniverseIdGenerator idGenerator() {
        return this.idGenerator;
    }

    /**
     * Returns the current modification count of this Universe.
     * Iterators and Flyweight Sets must snapshot this value upon creation and
     * validate against it during iteration to provide fail-fast concurrency.
     *
     * @return the current modification count
     */
    public long modCount() {
        return this.idGenerator.getModCount();
    }

    /**
     * Increments the Universe modification count.
     * Must be called whenever topology or global BitSet properties are altered.
     *
     * @return the incremented modification count
     */
    public long incrementModCount() {
        return this.idGenerator.incrementAndGetModCount();
    }

    // =========================================================================
    // 2. PHASE 4/5 ARCHITECTURAL STUBS
    // =========================================================================

    /**
     * Promotes a write-optimized EphemeralGraph into a read-optimized UniverseGraph.
     * Deep-clones state, translates negative IDs to positive IDs, rewires topology,
     * and permanently invalidates the ephemeral sandbox.
     *
     * @param ephemeral The sandbox graph to promote and invalidate.
     * @return A read-optimized, BitSet-backed view of the promoted topology.
     */
    public Graph promote(EphemeralGraph ephemeral) {
        throw new UnsupportedOperationException("TODO: Implement in Phase 4 (Promotion)");
    }

    // Note: Future columnar arrays (int[] edgeSources) and
    // inverted indices (Map<String, BitSet> nodeTags) will be placed here in Phase 4/5.
}
