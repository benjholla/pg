package dev.chpg.pg.multiverse.ephemeral;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

/** The ephemeral implementation of an Edge. */
public final class EphemeralEdge implements Edge {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;
    private Node from;
    private Node to;

    /**
     * Constructs a new EphemeralEdge.
     * @param id the edge id
     * @param from the source node
     * @param to the target node
     */
    public EphemeralEdge(int id, Node from, Node to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Edge endpoints cannot be null.");
        }
        this.id = id;
        this.tags = new EphemeralTagSet();
        this.attributes = new EphemeralAttributeMap();
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
        if (!(obj instanceof EphemeralEdge)) {
            return false;
        }
        EphemeralEdge other = (EphemeralEdge) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "EphemeralEdge [from=" + from + ", to=" + to + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
