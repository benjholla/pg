package dev.chpg.pg.api;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

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
        if (elements.isEmpty()) return Optional.empty();
        return Optional.of(elements.iterator().next());
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return EdgeSet.empty();
        }
        Set<Edge> intersected = new HashSet<>();
        for (Edge e : elements) {
            if (other.contains(e)) {
                intersected.add(e);
            }
        }
        return intersected.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(Collections.unmodifiableSet(intersected));
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Edge> differenced = new HashSet<>();
        for (Edge e : elements) {
            if (!other.contains(e)) {
                differenced.add(e);
            }
        }
        return differenced.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(Collections.unmodifiableSet(differenced));
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Edge> unioned = new HashSet<>((int) ((elements.size() + other.size()) / 0.75f) + 1);
        unioned.addAll(elements);
        unioned.addAll(other);
        return new GenericImmutableEdgeSet(Collections.unmodifiableSet(unioned));
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new HashSet<>((int) (elements.size() / 0.75f) + 1);
        for (Edge edge : elements) {
            ids.add(edge.id());
        }
        return Collections.unmodifiableSet(ids);
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[elements.size()];
        int i = 0;
        for (Edge edge : elements) {
            result[i++] = edge.id();
        }
        return result;
    }
}
