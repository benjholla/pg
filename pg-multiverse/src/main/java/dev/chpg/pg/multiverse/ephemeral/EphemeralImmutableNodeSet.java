package dev.chpg.pg.multiverse.ephemeral;

import java.util.Collection;
import java.util.Iterator;
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
 * An immutable, snapshot view of a collection of {@link Node} objects.
 * <p>
 * <b>What it represents:</b> A frozen, unmodifiable set of nodes tied to an ephemeral sandbox.
 * <p>
 * <b>Why it exists:</b> It fulfills the {@code ImmutableNodeSet} contract by permanently freezing a query result, ensuring that regardless of future graph mutations, the snapshot remains mathematically identical.
 * <p>
 * <b>When to use it:</b> Use it when you need to safely return, store, or share the result of an algebraic traversal (like {@code union} or {@code difference}) without risking external corruption.
 * <p>
 * <b>Common usage patterns:</b> It is typically generated internally by operations like {@code toImmutable()} on mutable or live node sets.
 * <p>
 * <b>Important invariants:</b> Attempts to modify this set will throw an {@code UnsupportedOperationException}.
 * <p>
 * <b>Thread safety:</b> Completely thread-safe. As an immutable structure, it can be safely shared across concurrent execution contexts.
 * <p>
 * <b>Performance characteristics:</b> Delegates read operations to its internal materialized set, providing O(1) lookups without any further allocation overhead.
 */
public class EphemeralImmutableNodeSet implements NodeSet {
    private final NodeSet nodes;
    
    /**
     * Constructs a new immutable wrapper around the provided materialized node set.
     *
     * @param nodes the internally materialized node set to freeze
     */
    public EphemeralImmutableNodeSet(NodeSet nodes) {
        this.nodes = nodes;
    }

    @Override
    public NodeSet materialize() {
        return this;
    }

    @Override
    public NodeSet toImmutable() {
        return this;
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
        throw new UnsupportedOperationException();
    }

    public boolean contains(Object obj) {
        return nodes.contains(obj);
    }

    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

        @Override
    public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public void clear() {
        throw new UnsupportedOperationException();
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
                throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public Spliterator<Node> spliterator() {
        return nodes.spliterator();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return nodes.toArray(generator);
    }

    public boolean removeIf(Predicate<? super Node> filter) {
        throw new UnsupportedOperationException();
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
