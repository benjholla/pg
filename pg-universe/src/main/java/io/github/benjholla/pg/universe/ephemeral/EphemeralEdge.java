package io.github.benjholla.pg.universe.ephemeral;

import io.github.benjholla.pg.api.AttributeMap;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.TagSet;

public final class EphemeralEdge implements Edge {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;
    private Node from;
    private Node to;

    public EphemeralEdge(Node from, Node to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Edge endpoints cannot be null.");
        }
        this.id = EphemeralIdGenerator.INSTANCE.create();
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
