package io.github.benjholla.pg.universe.ephemeral;

import io.github.benjholla.pg.api.AttributeMap;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.TagSet;

public final class EphemeralNode implements Node {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;

    public EphemeralNode(int id) {
        this.id = id;
        this.tags = new EphemeralTagSet();
        this.attributes = new EphemeralAttributeMap();
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
