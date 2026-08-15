package dev.chpg.pg.global;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

/**
 * A concrete implementation of {@link EdgeSet} designed for the global ID space.
 * <p>
 * <b>What it represents:</b> A mutable collection of uniquely identified {@link GlobalEdge}s.
 * <p>
 * <b>Why it exists:</b> To provide the underlying edge collection mechanism for {@link GlobalGraph}.
 * <p>
 * <b>When to use it:</b> Use this set when you need to construct or manipulate groups of edges belonging to a global graph.
 * <p>
 * <b>Important invariants:</b> This set violently rejects attempts to add foreign or incompatible edge implementations to prevent cross-graph contamination.
 */
public final class GlobalEdgeSet implements EdgeSet {

    private final HashSet<GlobalEdge> internalSet;
    private boolean isImmutable = false;

    /**
     * Constructs a new, empty {@code GlobalEdgeSet}.
     */
    public GlobalEdgeSet() {
        this.internalSet = new HashSet<>();
    }

    /**
     * Constructs a new {@code GlobalEdgeSet} containing the specified initial edge.
     *
     * @param initialEdge the edge to add
     */
    public GlobalEdgeSet(Edge initialEdge) {
        this();
        add(initialEdge);
    }

    /**
     * Constructs a new {@code GlobalEdgeSet} containing the elements of the specified array.
     *
     * @param initialEdges the array of initial edges
     */
    public GlobalEdgeSet(Edge... initialEdges) {
        Objects.requireNonNull(initialEdges, "Edge array cannot be null");
        this.internalSet = new HashSet<>((int) (initialEdges.length / 0.75f) + 1);
        for (Edge edge : initialEdges) {
            add(edge);
        }
    }

    /**
     * Constructs a new {@code GlobalEdgeSet} containing the elements of the specified collection.
     *
     * @param initialEdges the collection whose elements are to be placed into this set
     */
    public GlobalEdgeSet(Collection<Edge> initialEdges) {
        this();
        Objects.requireNonNull(initialEdges, "Edge collection cannot be null");
        addAll(initialEdges);
    }

    GlobalEdgeSet asSealed() {
        this.isImmutable = true;
        return this;
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
        if (isImmutable) { return this; }
        if (internalSet.isEmpty()) { return EdgeSet.empty(); }
        return internalSet.size() == 1 ? new GlobalImmutableSingletonEdgeSet(internalSet.iterator().next()) : new GlobalEdgeSet(this).asSealed();
    }

    @Override
    public Optional<Edge> one() {
        if (internalSet.isEmpty()) { return Optional.empty(); }
        return Optional.of(internalSet.iterator().next());
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        GlobalEdgeSet result = new GlobalEdgeSet();
        if (other == null || other.isEmpty()) {
            return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed());
        }
        for (GlobalEdge edge : internalSet) {
            if (other.contains(edge)) {
                result.internalSet.add(edge);
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed());
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        GlobalEdgeSet result = new GlobalEdgeSet();
        for (GlobalEdge edge : internalSet) {
            if (other == null || !other.contains(edge)) {
                result.internalSet.add(edge);
            }
        }
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed());
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
        return result.isEmpty() ? EdgeSet.empty() : (result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed());
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
        if (isImmutable) { throw new UnsupportedOperationException("Collection is unmodifiable"); }
        return internalSet.add(validate(edge));
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) { return false; }
        try {
            return internalSet.contains(validate(edge));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (isImmutable) { throw new UnsupportedOperationException("Collection is unmodifiable"); }
        if (!(obj instanceof Edge edge)) { return false; }
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
        if (isImmutable) { throw new UnsupportedOperationException("Collection is unmodifiable"); }
        internalSet.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Edge> iterator() {
        if (isImmutable) {
            return new Iterator<Edge>() {
                private final Iterator<GlobalEdge> it = internalSet.iterator();
                @Override public boolean hasNext() { return it.hasNext(); }
                @Override public Edge next() { return it.next(); }
                @Override public void remove() { throw new UnsupportedOperationException("Collection is unmodifiable"); }
            };
        }
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
        if (isImmutable) { throw new UnsupportedOperationException("Collection is unmodifiable"); }
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
        if (isImmutable) { throw new UnsupportedOperationException("Collection is unmodifiable"); }
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
        if (isImmutable) { throw new UnsupportedOperationException("Collection is unmodifiable"); }
        Objects.requireNonNull(c);
        boolean modified = false;
        for (Object obj : c) {
            modified |= this.remove(obj);
        }
        return modified;
    }

    @Override
    public boolean removeIf(java.util.function.Predicate<? super Edge> filter) {
        if (isImmutable) { throw new UnsupportedOperationException("Collection is unmodifiable"); }
        return internalSet.removeIf(filter);
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
        if (this == o) { return true; }
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
