package dev.chpg.pg.global;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

/**
 * A concrete implementation of a graph {@link Edge} designed for the global ID space.
 * <p>
 * <b>What it represents:</b> A directed connection between two nodes (identified by primitive IDs) that may carry tags and attributes.
 * <p>
 * <b>Why it exists:</b> To provide a robust, globally unique edge type for use within {@link GlobalGraph}.
 * <p>
 * <b>When to use it:</b> Use this when constructing topologies for {@link GlobalGraph} instances.
 */
public final class GlobalEdge implements Edge {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;
    private Node from;
    private Node to;

    /**
     * Constructs a new {@code GlobalEdge} from the specified source node to the target node.
     *
     * @param from the source node
     * @param to   the target node
     * @throws IllegalArgumentException if either endpoint is null
     */
    public GlobalEdge(Node from, Node to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Edge endpoints cannot be null.");
        }
        this.id = GlobalIdGenerator.INSTANCE.createEdgeId();
        this.tags = new GlobalTagSet();
        this.attributes = new GlobalAttributeMap();
        this.from = from;
        this.to = to;
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
    public Node from() {
        return from;
    }

    @Override
    public Node to() {
        return to;
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
        if (!(obj instanceof GlobalEdge)) {
            return false;
        }
        GlobalEdge other = (GlobalEdge) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "GlobalEdge [from=" + from + ", to=" + to + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
