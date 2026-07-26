package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

import java.util.Objects;

/**
 * A read-optimized Flyweight representing a persistent directed edge in the pg-multiverse.
 * Contains strictly zero local collections or state beyond its primitive ID.
 */
public final class UniverseEdge implements Edge, UniverseView {

    private final Universe universe;
    private final int id;

    /**
     * Package-private constructor.
     * Only the Universe (or Universe Sets) should instantiate these transient wrappers.
     */
    UniverseEdge(Universe universe, int id) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");

        if (id <= 0) {
            throw new IllegalArgumentException("UniverseEdge IDs must be strictly positive. Received: " + id);
        }
        this.id = id;
    }

    // =========================================================================
    // 1. PRIMITIVE IDENTITY
    // =========================================================================

    @Override
    public int id() {
        return this.id;
    }

    /**
     * Exposes the underlying bitwise storage engine backing this element.
     */
    @Override
    public Universe universe() {
        return this.universe;
    }

    // =========================================================================
    // 2. LAZY TOPOLOGY RESOLUTION
    // =========================================================================

    @Override
    public Node from() {
        // We do not store a Node reference. We ask the central engine for the
        // raw primitive integer from the edgeSources array, and wrap it on the fly.
        int sourceId = this.universe.edgeSource(this.id);
        return new UniverseNode(this.universe, sourceId);
    }

    @Override
    public Node to() {
        int targetId = this.universe.edgeTarget(this.id);
        return new UniverseNode(this.universe, targetId);
    }

    // =========================================================================
    // 3. EQUALITY & HASHING
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UniverseEdge)) {
            return false;
        }

        UniverseEdge that = (UniverseEdge) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }

    // =========================================================================
    // 4. PHASE 5 STUBS (Columnar Properties)
    // =========================================================================

    @Override
    public TagSet tags() {
        // TODO: Phase 5 - Return new UniverseTagProxy(this.universe, this.id);
        throw new UnsupportedOperationException("Columnar properties not yet implemented in Phase 2.");
    }

    @Override
    public AttributeMap attributes() {
        // TODO: Phase 5 - Return new UniverseAttributeProxy(this.universe, this.id);
        throw new UnsupportedOperationException("Columnar properties not yet implemented in Phase 2.");
    }
}
