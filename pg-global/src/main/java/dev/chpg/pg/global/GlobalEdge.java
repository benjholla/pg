package dev.chpg.pg.global;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

/**
 * undocumented.
 */
public final class GlobalEdge implements Edge {

    /**
     * undocumented.
     */
    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;
    private Node from;
    private Node to;

    /**
     * undocumented.
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
