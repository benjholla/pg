package io.github.benjholla.pg.universe.ephemeral;

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

public class EphemeralUnmodifiableEdgeSet implements EdgeSet {
    private final EdgeSet edges;

    public EphemeralUnmodifiableEdgeSet(EdgeSet edges) {
        this.edges = edges;
    }

    public Optional<Edge> one() {
        return edges.one();
    }

    public EdgeSet filter(String attribute) {
        return edges.filter(attribute);
    }

    public void forEach(Consumer<? super Edge> action) {
        edges.forEach(action);
    }

    public EdgeSet filter(String attribute, AttributeValue... values) {
        return edges.filter(attribute, values);
    }

    public boolean add(Edge edge) {
        throw new UnsupportedOperationException();
    }

    public boolean contains(Object obj) {
        return edges.contains(obj);
    }

    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return edges.size();
    }

    public boolean isEmpty() {
        return edges.isEmpty();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Iterator<Edge> iterator() {
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
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public Spliterator<Edge> spliterator() {
        return edges.spliterator();
    }

    public <T> T[] toArray(IntFunction<T[]> generator) {
        return edges.toArray(generator);
    }

    public boolean removeIf(Predicate<? super Edge> filter) {
        throw new UnsupportedOperationException();
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
