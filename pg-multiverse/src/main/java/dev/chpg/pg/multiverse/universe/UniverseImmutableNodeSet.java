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

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

/**
 * An immutable, snapshot view of a collection of {@link UniverseNode} objects.
 * <p>
 * <b>What it represents:</b> A frozen, unmodifiable set of nodes tied to a closed Universe.
 * <p>
 * <b>Why it exists:</b> It fulfills the {@code ImmutableNodeSet} contract by permanently freezing a query result, ensuring that regardless of future graph mutations, the snapshot remains mathematically identical.
 * <p>
 * <b>When to use it:</b> Use it when you need to safely return, store, or share the result of an algebraic traversal (like {@code union} or {@code difference}) without risking external corruption.
 * <p>
 * <b>Common usage patterns:</b> It is typically generated internally by operations like {@code toImmutable()} on mutable or live node sets.
 * <p>
 * <b>Important invariants:</b> Attempts to modify this set will throw an {@code UnsupportedOperationException}. It strictly maintains the {@code UniverseView} lineage of its delegate.
 * <p>
 * <b>Thread safety:</b> Completely thread-safe. As an immutable structure, it can be safely shared across concurrent execution contexts.
 * <p>
 * <b>Performance characteristics:</b> Delegates read operations and mathematical set operations to its internal materialized set, leveraging the underlying bitwise fast-paths without allocation overhead.
 */
public final class UniverseImmutableNodeSet implements NodeSet, UniverseView {

    private final Universe universe;
    private final NodeSet nodes;

    /**
     * Constructs a new immutable wrapper around the provided materialized node set.
     *
     * @param nodes the internally materialized node set to freeze
     * @throws IllegalArgumentException if the delegate is not a UniverseView
     */
    public UniverseImmutableNodeSet(NodeSet nodes) {
        this.nodes = Objects.requireNonNull(nodes, "NodeSet cannot be null");
        if (!(nodes instanceof UniverseView)) {
            throw new IllegalArgumentException("Delegate NodeSet must implement UniverseView to belong to a Universe.");
        }
        this.universe = ((UniverseView) nodes).universe();
    }

    @Override
    public Universe universe() {
        return this.universe;
    }

    @Override
    public NodeSet materialize() {
        return this;
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    @Override
    public boolean isSizeKnown() {
        return nodes.isSizeKnown();
    }

    public Optional<Node> one() {
        return nodes.one();
    }

    public void forEach(Consumer<? super Node> action) {
        nodes.forEach(action);
    }

    public NodeSet intersect(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return nodes.intersect(other);
    }

    public NodeSet difference(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return nodes.difference(other);
    }

    public NodeSet union(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return nodes.union(other);
    }

    public Set<Integer> ids() {
        return nodes.ids();
    }

    public int[] toIdArray() {
        return nodes.toIdArray();
    }

    public boolean add(Node node) {
        throw new UnsupportedOperationException("UniverseImmutableNodeSet is immutable.");
    }

    public boolean contains(Object obj) {
        return nodes.contains(obj);
    }

    public boolean remove(Object obj) {
        throw new UnsupportedOperationException("UniverseImmutableNodeSet is immutable.");
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public void clear() {
        throw new UnsupportedOperationException("UniverseImmutableNodeSet is immutable.");
    }

    public Iterator<Node> iterator() {
        // Preserve anonymous wrapper: Prevents iterator.remove() from bypassing graph mutation invariants or immutability contracts.
        return new Iterator<Node>() {
            private final Iterator<Node> it = nodes.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Node next() {
                return it.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("UniverseImmutableNodeSet iterator is read-only.");
            }
        };
    }

    public Object[] toArray() {
        return nodes.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return nodes.toArray(a);
    }

    public boolean containsAll(Collection<?> c) {
        return nodes.containsAll(c);
    }

    public boolean addAll(Collection<? extends Node> c) {
        throw new UnsupportedOperationException("UniverseImmutableNodeSet is immutable.");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseImmutableNodeSet is immutable.");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("UniverseImmutableNodeSet is immutable.");
    }

    public Spliterator<Node> spliterator() {
        return nodes.spliterator();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return nodes.toArray(generator);
    }

    public boolean removeIf(Predicate<? super Node> filter) {
        throw new UnsupportedOperationException("UniverseImmutableNodeSet is immutable.");
    }

    public Stream<Node> stream() {
        return nodes.stream();
    }

    public Stream<Node> parallelStream() {
        return nodes.parallelStream();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;

     }
        return nodes.equals(obj);
    }

    @Override
    public int hashCode() {
        return nodes.hashCode();
    }

    public String toString() {
        return nodes.toString();
    }
}
