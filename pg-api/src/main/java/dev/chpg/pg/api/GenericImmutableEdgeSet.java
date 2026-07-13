package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class GenericImmutableEdgeSet extends AbstractSet<Edge> implements EdgeSet {

    private final Set<Edge> elements;

    public GenericImmutableEdgeSet(Collection<? extends Edge> elements) {
        this.elements = Set.copyOf(elements);
    }

    @Override
    public EdgeSet materialize() {
        return this;
    }

    @Override
    public EdgeSet toImmutable() {
        return this;
    }
    @Override
public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return elements.size();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    public Iterator<Edge> iterator() {
        return elements.iterator();
    }

    @Override
    public Optional<Edge> one() {
        return elements.stream().findAny();
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return EdgeSet.empty();
        }
        Set<Edge> intersected = elements.stream()
            .filter(other::contains)
            .collect(Collectors.toUnmodifiableSet());
        return intersected.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(intersected);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Edge> differenced = elements.stream()
            .filter(e -> !other.contains(e))
            .collect(Collectors.toUnmodifiableSet());
        return differenced.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(differenced);
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Edge> unioned = java.util.stream.Stream.concat(elements.stream(), other.stream())
            .collect(Collectors.toUnmodifiableSet());
        return new GenericImmutableEdgeSet(unioned);
    }

    @Override
    public Set<Integer> ids() {
        return elements.stream().map(Edge::id).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public int[] toIdArray() {
        return elements.stream().mapToInt(Edge::id).toArray();
    }
}
