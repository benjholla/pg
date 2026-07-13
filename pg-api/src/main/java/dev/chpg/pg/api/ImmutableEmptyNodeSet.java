package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public final class ImmutableEmptyNodeSet extends AbstractSet<Node> implements NodeSet {

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<Node> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Optional<Node> one() {
        return Optional.empty();
    }

    @Override
    public NodeSet attributedWith(String attribute) {
        return this;
    }

    @Override
    public NodeSet attributedWith(String attribute, AttributeValue... values) {
        return this;
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        return this;
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        return this;
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (other instanceof NodeSet) {
            return ((NodeSet) other).toImmutable();
        }
        return new GenericImmutableNodeSet(other);
    }

    @Override
    public Set<Integer> ids() {
        return Collections.emptySet();
    }

    @Override
    public int[] toIdArray() {
        return new int[0];
    }

    @Override
    public NodeSet taggedWithAny(String... tags) {
        return this;
    }

    @Override
    public NodeSet taggedWithAll(String... tags) {
        return this;
    }
}
