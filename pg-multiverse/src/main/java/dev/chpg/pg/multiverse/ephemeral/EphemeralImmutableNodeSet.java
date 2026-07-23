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
 * undocumented.
 */
public class EphemeralImmutableNodeSet implements NodeSet {
    private final NodeSet nodes;
    
    /**
     * undocumented.
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

    /**
     * undocumented.
     */
    public Optional<Node> one() {
        return nodes.one();
    }

    /**
     * undocumented.
     */
    public void forEach(Consumer<? super Node> action) {
        nodes.forEach(action);
    }

    /**
     * undocumented.
     */
    public NodeSet intersect(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return nodes.intersect(other);
    }

    /**
     * undocumented.
     */
    public NodeSet difference(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return nodes.difference(other);
    }

    /**
     * undocumented.
     */
    public NodeSet union(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return nodes.union(other);
    }

    /**
     * undocumented.
     */
    public Set<Integer> ids() {
        return nodes.ids();
    }

    /**
     * undocumented.
     */
    public int[] toIdArray() {
        return nodes.toIdArray();
    }

    /**
     * undocumented.
     */
    public boolean add(Node node) {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public boolean contains(Object obj) {
        return nodes.contains(obj);
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
        return nodes.size();
    }

    /**
     * undocumented.
     */
    public boolean isEmpty() {
        return nodes.isEmpty();
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

    /**
     * undocumented.
     */
    public Object[] toArray() {
        return nodes.toArray();
    }

    /**
     * undocumented.
     */
    public <T> T[] toArray(T[] a) {
        return nodes.toArray(a);
    }

    /**
     * undocumented.
     */
    public boolean containsAll(Collection<?> c) {
        return nodes.containsAll(c);
    }

    /**
     * undocumented.
     */
    public boolean addAll(Collection<? extends Node> c) {
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
    public Spliterator<Node> spliterator() {
        return nodes.spliterator();
    }

    /**
     * undocumented.
     */
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return nodes.toArray(generator);
    }

    /**
     * undocumented.
     */
    public boolean removeIf(Predicate<? super Node> filter) {
        throw new UnsupportedOperationException();
    }

    /**
     * undocumented.
     */
    public Stream<Node> stream() {
        return nodes.stream();
    }

    /**
     * undocumented.
     */
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
    
    /**
     * undocumented.
     */
    public String toString() {
        return nodes.toString();
    }
}
