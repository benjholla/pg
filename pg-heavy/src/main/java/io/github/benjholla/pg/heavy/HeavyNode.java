package io.github.benjholla.pg.heavy;

import io.github.benjholla.pg.api.AttributeMap;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.TagSet;

public final class HeavyNode implements Node {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;

    public HeavyNode() {
        this.id = HeavyIdGenerator.INSTANCE.createNodeId();
        this.tags = new HeavyTagSet();
        this.attributes = new HeavyAttributeMap();
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
        if (!(obj instanceof HeavyNode)) {
            return false;
        }
        HeavyNode other = (HeavyNode) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        return "HeavyNode [ attributes=" + this.attributes().toString() + ", tags=" + this.tags().toString() + "]";
    }

}
