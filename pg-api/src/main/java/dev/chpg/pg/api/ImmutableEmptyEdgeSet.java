package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public final class ImmutableEmptyEdgeSet extends AbstractSet<Edge> implements EdgeSet {

    @Override
    public EdgeSet materialize() {
        return this;
    }

    @Override
    public EdgeSet toImmutable() {
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
    public Iterator<Edge> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public Optional<Edge> one() {
        return Optional.empty();
    }

    @Override
    public EdgeSet withAttribute(String attribute) {
        return this;
    }

    @Override
    public EdgeSet withAttribute(String attribute, AttributeValue... values) {
        return this;
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        return this;
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        return this;
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (other instanceof EdgeSet) {
            return ((EdgeSet) other).toImmutable();
        }
        return new GenericImmutableEdgeSet(other);
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
    public EdgeSet withAnyTag(String... tags) {
        return this;
    }

    @Override
    public EdgeSet withAllTags(String... tags) {
        return this;
    }
}
