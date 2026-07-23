package dev.chpg.pg.global;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.EdgeSet;

/**
 * undocumented.
 */
public class GlobalImmutableEdgeSet implements EdgeSet {
    private final EdgeSet edges;

    /**
     * undocumented.
     */
    public GlobalImmutableEdgeSet(EdgeSet edges) {
        this.edges = edges;
    }

    @Override
    public EdgeSet materialize() {
        return this;
    }

    @Override
    public EdgeSet toImmutable() {
        return this;
    }

    /**
     * undocumented.
     */
    public Optional<Edge> one() {
        return edges.one();
    }

    /**
     * undocumented.
     */
    public void forEach(Consumer<? super Edge> action) {
        edges.forEach(action);
    }

    /**
     * undocumented.
     */
    public EdgeSet intersect(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return edges.intersect(other);
    }

    /**
     * undocumented.
     */
    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return edges.difference(other);
    }

    /**
     * undocumented.
     */
    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return edges.union(other);
    }

    /**
     * undocumented.
     */
    public Set<Integer> ids() {
        return edges.ids();
    }

    /**
     * undocumented.
     */
    public int[] toIdArray() {
        return edges.toIdArray();
    }

    /**
     * undocumented.
     */
    public boolean add(Edge edge) {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public boolean contains(Object obj) {
        return edges.contains(obj);
    }

    /**
     * undocumented.
     */
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

        @Override
    public boolean isMaterialized() {
        return true;
    }

    /**
     * undocumented.
     */
    public int size() {
        return edges.size();
    }

    /**
     * undocumented.
     */
    public boolean isEmpty() {
        return edges.isEmpty();
    }

    /**
     * undocumented.
     */
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public Iterator<Edge> iterator() {
        // Preserve anonymous wrapper: Prevents iterator.remove() from bypassing graph mutation invariants or immutability contracts.
        return new Iterator<Edge>() {
            private final Iterator<Edge> it = edges.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Edge next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * undocumented.
     */
    public Object[] toArray() {
        return edges.toArray();
    }

    /**
     * undocumented.
     */
    public <T> T[] toArray(T[] a) {
        return edges.toArray(a);
    }

    /**
     * undocumented.
     */
    public boolean containsAll(Collection<?> c) {
        return edges.containsAll(c);
    }

    /**
     * undocumented.
     */
    public boolean addAll(Collection<? extends Edge> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public Spliterator<Edge> spliterator() {
        return edges.spliterator();
    }

    /**
     * undocumented.
     */
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return edges.toArray(generator);
    }

    /**
     * undocumented.
     */
    public boolean removeIf(Predicate<? super Edge> filter) {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public Stream<Edge> stream() {
        return edges.stream();
    }

    /**
     * undocumented.
     */
    public Stream<Edge> parallelStream() {
        return edges.parallelStream();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return edges.equals(obj);
    }

    @Override
    public int hashCode() {
        return edges.hashCode();
    }

    /**
     * undocumented.
     */
    public String toString() {
        return edges.toString();
    }
}
