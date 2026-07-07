package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.AttributeMap;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.TagSet;

public final class HeavyEdge implements Edge {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;
    private Node from;
    private Node to;

    public HeavyEdge(Node from, Node to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Edge endpoints cannot be null.");
        }
        this.id = HeavyIdGenerator.INSTANCE.create();
        this.tags = new HeavyTagSet();
        this.attributes = new HeavyAttributeMap();
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
        if (!(obj instanceof HeavyEdge)) {
            return false;
        }
        HeavyEdge other = (HeavyEdge) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "HeavyEdge [from=" + from + ", to=" + to + ", attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
