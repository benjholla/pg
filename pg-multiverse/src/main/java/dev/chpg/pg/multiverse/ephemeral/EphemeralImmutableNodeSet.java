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

import dev.chpg.pg.api.AttributeValue;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

public class EphemeralImmutableNodeSet implements NodeSet {
    private final NodeSet nodes;
    
    public EphemeralImmutableNodeSet(NodeSet nodes) {
        this.nodes = nodes;
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    public Optional<Node> one() {
        return nodes.one();
    }

    public NodeSet filter(String attribute) {
        return nodes.filter(attribute);
    }

    public void forEach(Consumer<? super Node> action) {
        nodes.forEach(action);
    }

    public NodeSet filter(String attribute, AttributeValue... values) {
        return nodes.filter(attribute, values);
    }

    public NodeSet intersect(Collection<? extends Node> other) {
        return nodes.intersect(other);
    }

    public NodeSet difference(Collection<? extends Node> other) {
        return nodes.difference(other);
    }

    public NodeSet union(Collection<? extends Node> other) {
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
    

    @Override
    public NodeSet taggedWithAny(String... tags) {
        return nodes.taggedWithAny(tags);
    }

    @Override
    public NodeSet taggedWithAll(String... tags) {
        return nodes.taggedWithAll(tags);
    }
}
