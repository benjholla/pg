package dev.chpg.pg.multiverse.universe;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import java.util.Objects;

/**
 * A read-optimized Flyweight representing a persistent node in the pg-multiverse.
 * Contains strictly zero local collections or state beyond its primitive ID.
 */
public final class UniverseNode implements Node, UniverseView {

    private final Universe universe;
    private final int id;

    /**
     * Package-private or public constructor depending on your module boundaries.
     * Only the Universe (or Universe Sets) should instantiate these transient wrappers.
     *
     * @param universe the backing universe
     * @param id the primitive ID
     */
    public UniverseNode(Universe universe, int id) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");

        if (id <= 0) {
            throw new IllegalArgumentException("UniverseNode IDs must be strictly positive. Received: " + id);
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
    // 2. EQUALITY & HASHING (Architectural Mandate)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        // 1. Fast-path reference check
        if (this == o) {
            return true;
        }

        // 2. Strict type check (rejects EphemeralNodes and GlobalNodes instantly)
        if (!(o instanceof UniverseNode)) {
            return false;
        }

        // 3. Primitive identity check
        UniverseNode that = (UniverseNode) o;
        return this.id == that.id;

        // Note: We do not check this.universe == that.universe in equals().
        // Cross-universe contamination is protected at the traversal/query boundaries,
        // not the base identity level.
    }

    @Override
    public int hashCode() {
        // Relies purely on the primitive ID to avoid object-header overhead
        return Integer.hashCode(this.id);
    }

    // =========================================================================
    // 3. PHASE 5 STUBS (Columnar Properties)
    // =========================================================================

    @Override
    public TagSet tags() {
        return new UniverseNodeTagProxy(this.universe, this.id);
    }

    @Override
    public AttributeMap attributes() {
        return new UniverseNodeAttributeProxy(this.universe, this.id);
    }

    @Override
    public String toString() {
        return "UniverseNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }
}
