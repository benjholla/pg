package dev.chpg.pg.global;

import dev.chpg.pg.api.AttributeMap;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.TagSet;

public final class GlobalNode implements Node {

    private final int id;
    private final TagSet tags;
    private final AttributeMap attributes;

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
