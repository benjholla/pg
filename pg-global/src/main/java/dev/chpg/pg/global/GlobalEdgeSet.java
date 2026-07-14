package dev.chpg.pg.global;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

public final class GlobalEdgeSet implements EdgeSet {

    private final HashSet<GlobalEdge> internalSet;

    public GlobalEdgeSet() {
        this.internalSet = new HashSet<>();
    }

    public GlobalEdgeSet(Edge initialEdge) {
        this();
        add(initialEdge);
    }

    public GlobalEdgeSet(Edge... initialEdges) {
        this();
        Objects.requireNonNull(initialEdges, "Edge array cannot be null");
        addAll(Arrays.asList(initialEdges));
    }

    public GlobalEdgeSet(Collection<Edge> initialEdges) {
        this();
        Objects.requireNonNull(initialEdges, "Edge collection cannot be null");
        addAll(initialEdges);
    }

    private GlobalEdge validate(Edge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        if (!(edge instanceof GlobalEdge impl)) {
            throw new IllegalArgumentException(
                "Cross-graph contamination: Expected GlobalEdge, got " + edge.getClass().getSimpleName()
            );
        }
        return impl;
    }

    @Override
    public EdgeSet toImmutable() {
        if (internalSet.isEmpty()) return EdgeSet.empty();
        if (internalSet.size() == 1) return new GlobalImmutableSingletonEdgeSet(internalSet.iterator().next());
        return new GlobalImmutableEdgeSet(new GlobalEdgeSet(this));
    }

    @Override
    public Optional<Edge> one() {
        return internalSet.stream().map(e -> (Edge) e).findAny();
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        GlobalEdgeSet result = new GlobalEdgeSet();
        if (other == null || other.isEmpty()) {
            return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : new GlobalImmutableEdgeSet(result));
        }
        for (GlobalEdge edge : internalSet) {
            if (other.contains(edge)) {
                result.internalSet.add(edge);
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : new GlobalImmutableEdgeSet(result));
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        GlobalEdgeSet result = new GlobalEdgeSet();
        for (GlobalEdge edge : internalSet) {
            if (other == null || !other.contains(edge)) {
                result.internalSet.add(edge);
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : new GlobalImmutableEdgeSet(result));
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        GlobalEdgeSet result = new GlobalEdgeSet();
        result.internalSet.addAll(this.internalSet);
        if (other != null) {
            for (Edge e : other) {
                if (e instanceof GlobalEdge ge) {
                    result.internalSet.add(ge);
                }
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : new GlobalImmutableEdgeSet(result));
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new HashSet<>((int) (internalSet.size() / 0.75f) + 1);
        for (GlobalEdge edge : internalSet) {
            ids.add(edge.id());
        }
        return ids;
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[internalSet.size()];
        int i = 0;
        for (Edge edge : internalSet) {
            result[i++] = edge.id();
        }
        return result;
    }

    @Override
    public boolean add(Edge edge) {
        return internalSet.add(validate(edge));
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) return false;
        try {
            return internalSet.contains(validate(edge));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Edge edge)) return false;
        try {
            return internalSet.remove(validate(edge));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    @Override
public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return internalSet.size();
    }

    @Override
    public boolean isEmpty() {
        return internalSet.isEmpty();
    }

    @Override
    public void clear() {
        internalSet.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Edge> iterator() {
        return (Iterator<Edge>) (Iterator<?>) internalSet.iterator();
    }

    @Override
    public Object[] toArray() {
        return internalSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return internalSet.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c);
        for (Object obj : c) {
            if (!contains(obj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Edge> c) {
        Objects.requireNonNull(c, "Edge collection cannot be null");
        for (Edge e : c) {
            validate(e);
        }
        boolean modified = false;
        for (Edge e : c) {
            modified |= internalSet.add((GlobalEdge) e);
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<GlobalEdge> it = internalSet.iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        for (Object obj : c) {
            modified |= this.remove(obj);
        }
        return modified;
    }

    @Override
    public String toString() {
        String joined = internalSet.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
        return "GlobalEdgeSet [edges=" + joined + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Standard Java semantics: safely compares sizes and elements,
        // evaluating to true for empty sets of different types,
        // while deferring to elements for populated sets.
        return internalSet.equals(o);
    }

    @Override
    public int hashCode() {
        return internalSet.hashCode();
    }
}
