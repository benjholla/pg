package io.github.benjholla.pg.global;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class GlobalImmutableSingletonNodeSet extends AbstractSet<Node> implements NodeSet {

    private final GlobalNode element;

    public GlobalImmutableSingletonNodeSet(GlobalNode element) {
        this.element = Objects.requireNonNull(element, "element cannot be null");
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(Object o) {
        return element.equals(o);
    }

    @Override
    public Iterator<Node> iterator() {
        return Collections.<Node>singleton(element).iterator();
    }

    @Override
    public Optional<Node> one() {
        return Optional.of(element);
    }

    @Override
    public NodeSet filter(String attribute) {
        if (element.attributes().containsKey(attribute)) {
            return this;
        }
        return NodeSet.empty(); // return empty node set
    }

    @Override
    public NodeSet filter(String attribute, AttributeValue... values) {
        if (element.attributes().containsKey(attribute)) {
            AttributeValue attrValue = element.attributes().get(attribute);
            for (AttributeValue v : values) {
                if (attrValue.equals(v)) {
                    return this;
                }
            }
        }
        return NodeSet.empty(); // return empty node set
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        if (other.contains(element)) {
            return this;
        }
        return NodeSet.empty();
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        if (other.contains(element)) {
            return NodeSet.empty();
        }
        return this;
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        GlobalNodeSet result = new GlobalNodeSet();
        result.add(element);
        for (Node n : other) {
            result.add((GlobalNode) n);
        }
        if (result.size() == 1) {
            return this;
        }
        return new GlobalImmutableNodeSet(result);
    }

    @Override
    public Set<Integer> ids() {
        return Collections.singleton(element.id());
    }

    @Override
    public int[] toIdArray() {
        return new int[]{element.id()};
    }

    @Override
    public NodeSet taggedWithAny(String... tags) {
        if (tags != null && tags.length > 0) {
            for (String tag : tags) {
                if (element.tags().contains(tag)) {
                    return this;
                }
            }
        }
        return NodeSet.empty();
    }

    @Override
    public NodeSet taggedWithAll(String... tags) {
        if (tags != null && tags.length > 0) {
            for (String tag : tags) {
                if (!element.tags().contains(tag)) {
                    return NodeSet.empty();
                }
            }
            return this;
        }
        return NodeSet.empty();
    }
}
