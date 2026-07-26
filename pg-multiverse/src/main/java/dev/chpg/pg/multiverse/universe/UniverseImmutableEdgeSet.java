package dev.chpg.pg.multiverse.universe;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
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
 * An immutable, snapshot view of a collection of {@link UniverseEdge} objects.
 * <p>
 * <b>What it represents:</b> A frozen, unmodifiable set of edges tied to a closed Universe.
 * <p>
 * <b>Why it exists:</b> It fulfills the {@code ImmutableEdgeSet} contract by permanently freezing a query result, ensuring that regardless of future graph mutations, the snapshot remains mathematically identical.
 * <p>
 * <b>When to use it:</b> Use it when you need to safely return, store, or share the result of an algebraic traversal (like {@code union} or {@code difference}) without risking external corruption.
 * <p>
 * <b>Common usage patterns:</b> It is typically generated internally by operations like {@code toImmutable()} on mutable or live edge sets.
 * <p>
 * <b>Important invariants:</b> Attempts to modify this set will throw an {@code UnsupportedOperationException}. It strictly maintains the {@code UniverseView} lineage of its delegate.
 * <p>
 * <b>Thread safety:</b> Completely thread-safe. As an immutable structure, it can be safely shared across concurrent execution contexts.
 * <p>
 * <b>Performance characteristics:</b> Delegates read operations and mathematical set operations to its internal materialized set, leveraging the underlying bitwise fast-paths without allocation overhead.
 */
public final class UniverseImmutableEdgeSet implements EdgeSet, UniverseView {

    private final Universe universe;
    private final EdgeSet edges;

    /**
     * Constructs a new immutable wrapper around the provided materialized edge set.
     *
     * @param edges the internally materialized edge set to freeze
     * @throws IllegalArgumentException if the delegate is not a UniverseView
     */
    public UniverseImmutableEdgeSet(EdgeSet edges) {
        this.edges = Objects.requireNonNull(edges, "EdgeSet cannot be null");
        if (!(edges instanceof UniverseView)) {
            throw new IllegalArgumentException("Delegate EdgeSet must implement UniverseView to belong to a Universe.");
        }
        this.universe = ((UniverseView) edges).universe();
    }

    @Override
    public Universe universe() {
        return this.universe;
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

    @Override
    public boolean isSizeKnown() {
        return edges.isSizeKnown();
    }

    public Optional<Edge> one() {
        return edges.one();
    }

    public void forEach(Consumer<? super Edge> action) {
        edges.forEach(action);
    }

    public EdgeSet intersect(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return edges.intersect(other);
    }

    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return edges.difference(other);
    }

    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return edges.union(other);
    }

    public Set<Integer> ids() {
        return edges.ids();
    }

    public int[] toIdArray() {
        return edges.toIdArray();
    }

    public boolean add(Edge edge) {
        throw new UnsupportedOperationException("UniverseImmutableEdgeSet is immutable.");
    }

    public boolean contains(Object obj) {
        return edges.contains(obj);
    }

    public boolean remove(Object obj) {
        throw new UnsupportedOperationException("UniverseImmutableEdgeSet is immutable.");
    }

    public int size() {
        return edges.size();
    }

    public boolean isEmpty() {
        return edges.isEmpty();
    }

    public void clear() {
        throw new UnsupportedOperationException("UniverseImmutableEdgeSet is immutable.");
    }

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
                throw new UnsupportedOperationException("UniverseImmutableEdgeSet iterator is read-only.");
            }
        };
    }

    public Object[] toArray() {
        return edges.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return edges.toArray(a);
    }

    public boolean containsAll(Collection<?> c) {
        return edges.containsAll(c);
    }

    public boolean addAll(Collection<? extends Edge> c) {
        throw new UnsupportedOperationException("UniverseImmutableEdgeSet is immutable.");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseImmutableEdgeSet is immutable.");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseImmutableEdgeSet is immutable.");
    }

    public Spliterator<Edge> spliterator() {
        return edges.spliterator();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return edges.toArray(generator);
    }

    public boolean removeIf(Predicate<? super Edge> filter) {
        throw new UnsupportedOperationException("UniverseImmutableEdgeSet is immutable.");
    }

    public Stream<Edge> stream() {
        return edges.stream();
    }

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

    public String toString() {
        return edges.toString();
    }
}
