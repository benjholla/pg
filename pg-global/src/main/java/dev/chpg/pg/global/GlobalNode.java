package dev.chpg.pg.global;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

/**
 * Represents a lightweight node within a {@code GlobalGraph}.
 * <p>
 * <b>What it represents:</b> A concrete, single vertex in the primary `GlobalGraph` data store.
 * <p>
 * <b>Why it exists:</b> It provides a fast, heap-friendly vertex structure with a globally unique dense ID for efficient O(1) adjacency lookups using integer arrays.
 * <p>
 * <b>When to use it:</b> Use this when constructing a core in-memory graph where scaling to millions of permanent elements is required and structural mutability is necessary.
 * <p>
 * <b>Common usage patterns:</b> It is generally instantiated implicitly by a factory or graph implementation, and manipulated directly using the inherited `tags()` and `attributes()` interfaces.
 * <p>
 * <b>Important invariants:</b> The ID assigned to this node is strictly positive, globally unique per `GlobalGraph` sequence, and monotonically increasing.
 * <p>
 * <b>Thread safety:</b> Not inherently thread-safe. Synchronization is necessary if multiple threads mutate its tags or attributes concurrently.
 * <p>
 * <b>Performance characteristics:</b> Identity checks, hashing, and topological equality resolve in O(1) time strictly via primitive ID comparison without traversing its internal collections.
 */
public final class GlobalNode implements Node {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;

    /**
     * Constructs a new {@code GlobalNode} with a globally unique ID generated dynamically.
     */
    public GlobalNode() {
        this.id = GlobalIdGenerator.INSTANCE.createNodeId();
        this.tags = new GlobalTagSet();
        this.attributes = new GlobalAttributeMap();
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
        if (!(obj instanceof GlobalNode)) {
            return false;
        }
        GlobalNode other = (GlobalNode) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "GlobalNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
