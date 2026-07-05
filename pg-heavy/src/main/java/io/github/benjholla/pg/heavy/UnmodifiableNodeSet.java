package io.github.benjholla.pg.heavy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class UnmodifiableNodeSet implements NodeSet {
    private final NodeSet nodes;
    
    public UnmodifiableNodeSet(NodeSet nodes) {
        this.nodes = nodes;
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

    public boolean add(Node node) {
        return nodes.add(node);
    }

    public boolean contains(Object obj) {
        return nodes.contains(obj);
    }

    public boolean remove(Object obj) {
        return nodes.remove(obj);
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public void clear() {
        nodes.clear();
    }

    public Iterator<Node> iterator() {
        return nodes.iterator();
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
        return nodes.addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return nodes.retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return nodes.removeAll(c);
    }

    public Spliterator<Node> spliterator() {
        return nodes.spliterator();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return nodes.toArray(generator);
    }

    public boolean removeIf(Predicate<? super Node> filter) {
        return nodes.removeIf(filter);
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
