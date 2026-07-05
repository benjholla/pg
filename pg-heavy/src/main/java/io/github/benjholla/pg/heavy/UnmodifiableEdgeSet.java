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
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;

public class UnmodifiableEdgeSet implements EdgeSet {
    private final EdgeSet nodes;
    
    public UnmodifiableEdgeSet(EdgeSet nodes) {
        this.nodes = nodes;
    }

    public Optional<Edge> one() {
        return nodes.one();
    }

    public EdgeSet filter(String attribute) {
        return nodes.filter(attribute);
    }

    public void forEach(Consumer<? super Edge> action) {
        nodes.forEach(action);
    }

    public EdgeSet filter(String attribute, AttributeValue... values) {
        return nodes.filter(attribute, values);
    }

    public boolean add(Edge node) {
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

    public Iterator<Edge> iterator() {
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

    public boolean addAll(Collection<? extends Edge> c) {
        return nodes.addAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return nodes.retainAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return nodes.removeAll(c);
    }

    public Spliterator<Edge> spliterator() {
        return nodes.spliterator();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return nodes.toArray(generator);
    }

    public boolean removeIf(Predicate<? super Edge> filter) {
        return nodes.removeIf(filter);
    }

    public Stream<Edge> stream() {
        return nodes.stream();
    }

    public Stream<Edge> parallelStream() {
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
