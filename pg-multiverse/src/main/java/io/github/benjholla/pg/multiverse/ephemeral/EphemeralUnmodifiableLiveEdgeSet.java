package io.github.benjholla.pg.multiverse.ephemeral;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;

public class EphemeralUnmodifiableLiveEdgeSet implements EdgeSet {

    private final Map<Integer, EphemeralEdge> edges;

    public EphemeralUnmodifiableLiveEdgeSet(
            Map<Integer, EphemeralNode> nodes,
            Map<Integer, EphemeralEdge> edges,
            Map<Integer, EphemeralEdgeSet> inEdges,
            Map<Integer, EphemeralEdgeSet> outEdges) {
        this.edges = Objects.requireNonNull(edges);
    }

    @Override
    public EdgeSet toImmutable() {
        if (edges.isEmpty()) return EdgeSet.empty();
        if (edges.size() == 1) return new EphemeralImmutableSingletonEdgeSet(edges.values().iterator().next());
        EphemeralEdgeSet copy = new EphemeralEdgeSet();
        copy.addAll(edges.values());
        return new EphemeralImmutableEdgeSet(copy);
    }

    @Override
    public Optional<Edge> one() {
        return edges.values().stream().map(e -> (Edge) e).findAny();
    }

    @Override
    public EdgeSet filter(String attribute) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        for (EphemeralEdge edge : edges.values()) {
            if (edge.attributes().containsKey(attribute)) {
                result.add(edge);
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result);
    }

    @Override
    public EdgeSet filter(String attribute, AttributeValue... values) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        if (attribute != null && values != null) {
            for (EphemeralEdge edge : edges.values()) {
                AttributeValue attributeValue = edge.attributes().get(attribute);
                if (attributeValue != null) {
                    for (AttributeValue value : values) {
                        if (value != null) {
                            if (Objects.equals(attributeValue, value)) {
                                result.add(edge);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result);
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        if (other == null || other.isEmpty()) {
            return result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result);
        }
        for (EphemeralEdge edge : edges.values()) {
            if (other.contains(edge)) {
                result.add(edge);
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result);
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        EphemeralEdgeSet result = new EphemeralEdgeSet();
        for (EphemeralEdge edge : edges.values()) {
            if (other == null || !other.contains(edge)) {
                result.add(edge);
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result);
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        EphemeralEdgeSet result = new EphemeralEdgeSet(); result.addAll(edges.values());
        if (other != null) {
            for (Edge e : other) {
                if (e instanceof EphemeralEdge) {
                    result.add(e);
                }
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonEdgeSet((EphemeralEdge) result.iterator().next()) : new EphemeralImmutableEdgeSet(result);
    }

    @Override
    public Set<Integer> ids() {
        return java.util.Collections.unmodifiableSet(edges.keySet());
    }

    @Override
    public int[] toIdArray() {
        return edges.keySet().stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public boolean add(Edge edge) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof EphemeralEdge ge)) return false;
        return edges.containsKey(ge.id()) && edges.get(ge.id()).equals(ge);
    }

    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return edges.size();
    }

    @Override
    public boolean isEmpty() {
        return edges.isEmpty();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<EphemeralEdge> it = edges.values().iterator();
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

    @Override
    public Object[] toArray() {
        return edges.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return edges.values().toArray(a);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super Edge> action) {
        edges.values().forEach(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<Edge> spliterator() {
        return (Spliterator<Edge>) (Spliterator<?>) edges.values().spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return edges.values().toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super Edge> filter) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<Edge> stream() {
        return (Stream<Edge>) (Stream<?>) edges.values().stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<Edge> parallelStream() {
        return (Stream<Edge>) (Stream<?>) edges.values().parallelStream();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Set)) {
            return false;
        }
        Set<?> other = (Set<?>) obj;
        if (other.size() != size()) {
            return false;
        }
        return containsAll(other);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (EphemeralEdge edge : edges.values()) {
            if (edge != null) {
                h += edge.hashCode();
            }
        }
        return h;
    }

    @Override
    public String toString() {
        return edges.values().toString();
    }
}