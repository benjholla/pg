package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;
import dev.chpg.pg.multiverse.universe.Universe;
import dev.chpg.pg.multiverse.universe.UniverseView;
import java.util.Objects;

/**
 * Represents a transient, lightweight node within an {@code EphemeralGraph} sandbox.
 * <p>
 * <b>What it represents:</b> A concrete, single vertex in a high-speed mutation scratchpad.
 * <p>
 * <b>Why it exists:</b> It acts as a naked coordinate mapping to a negative integer sequence, providing zero-GC overhead and perfect mathematical isolation from permanent global records during intermediate computations.
 * <p>
 * <b>When to use it:</b> Use this when parsing, staging data, or performing local graph mutations before promoting the topology to a permanent registry.
 * <p>
 * <b>Common usage patterns:</b> Instantiated by an ephemeral ID generator and manipulated via the standard `tags()` and `attributes()` interfaces.
 * <p>
 * <b>Important invariants:</b> The ID must be strictly negative, enforcing the firewall against permanent {@code UniverseGraph} elements. This object does not maintain a backward reference to its parent graph.
 * <p>
 * <b>Thread safety:</b> Not thread-safe. Concurrent structural or state mutations must be synchronized.
 * <p>
 * <b>Performance characteristics:</b> Relies on standard HashMaps for property lookups, incurring slight hashing overhead compared to raw array layouts, which is the mathematically necessary trade-off for dynamic isolation.
 */
public final class EphemeralNode implements Node, UniverseView {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;



    private final Universe universe;

    /**
     * Constructs a new {@code EphemeralNode} with the specified negative identifier.
     * @param universe the target universe
     * @param id the strictly negative unique identifier for this node
     */
    public EphemeralNode(Universe universe, int id) {
        this.universe = Objects.requireNonNull(universe, "Universe cannot be null");
        this.id = id;
        this.tags = new EphemeralTagSet();
        this.attributes = new EphemeralAttributeMap();
    }

    @Override
    public Universe universe() {
        return universe;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public TagSet tags() {
        return tags;
    }

    @Override
    public AttributeMap attributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EphemeralNode)) {
            return false;
        }
        EphemeralNode other = (EphemeralNode) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "EphemeralNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
