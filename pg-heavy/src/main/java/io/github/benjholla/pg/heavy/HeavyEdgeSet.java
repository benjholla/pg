package io.github.benjholla.pg.heavy;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;

public class HeavyEdgeSet implements EdgeSet {

    private final HashSet<HeavyEdge> internalSet;

    public HeavyEdgeSet() {
        this.internalSet = new HashSet<>();
    }

    public HeavyEdgeSet(Edge initialEdge) {
        this();
        add(initialEdge);
    }

    public HeavyEdgeSet(Edge... initialEdges) {
        this();
        Objects.requireNonNull(initialEdges, "Edge array cannot be null");
        addAll(Arrays.asList(initialEdges));
    }

    public HeavyEdgeSet(Collection<Edge> initialEdges) {
        this();
        Objects.requireNonNull(initialEdges, "Edge collection cannot be null");
        addAll(initialEdges);
    }

    private HeavyEdge validate(Edge edge) {
        Objects.requireNonNull(edge, "Edge cannot be null");
        if (!(edge instanceof HeavyEdge impl)) {
            throw new IllegalArgumentException(
                "Cross-graph contamination: Expected HeavyEdge, got " + edge.getClass().getSimpleName()
            );
        }
        return impl;
    }

    @Override
    public Optional<Edge> one() {
        return internalSet.stream().map(e -> (Edge) e).findAny();
    }

    @Override
    public EdgeSet filter(String attribute) {
        HeavyEdgeSet result = new HeavyEdgeSet();
        for (HeavyEdge edge : internalSet) {
           if (edge.attributes().containsKey(attribute)) {
                result.internalSet.add(edge);
            }
        }
        return result;
    }

    @Override
    public EdgeSet filter(String attribute, AttributeValue... values) {
        HeavyEdgeSet result = new HeavyEdgeSet();
        if (attribute != null && values != null) {
            for (HeavyEdge edge : internalSet) {
               AttributeValue attributeValue = edge.attributes().get(attribute);
                if (attributeValue != null) {
                    for (AttributeValue value : values) {
                        if (value != null) {
                            if (Objects.equals(attributeValue, value)) {
                                result.internalSet.add(edge);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean add(Edge edge) {
        return internalSet.add(validate(edge));
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Edge edge)) return false;
        try {
            return internalSet.contains(validate(edge));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Edge edge)) return false;
        try {
            return internalSet.remove(validate(edge));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public int size() {
        return internalSet.size();
    }

    @Override
    public boolean isEmpty() {
        return internalSet.isEmpty();
    }

    @Override
    public void clear() {
        internalSet.clear();
    }

    @Override
    public Iterator<Edge> iterator() {
        Iterator<HeavyEdge> internalIterator = internalSet.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() { return internalIterator.hasNext(); }
            @Override
            public Edge next() { return internalIterator.next(); }
            @Override
            public void remove() { internalIterator.remove(); }
        };
    }

    @Override
    public Object[] toArray() {
        return internalSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return internalSet.toArray(a);
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
        Objects.requireNonNull(c, "Edge collection cannot be null");
        for (Edge e : c) {
            validate(e);
        }
        boolean modified = false;
        for (Edge e : c) {
            modified |= internalSet.add((HeavyEdge) e);
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<HeavyEdge> it = internalSet.iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        for (Object obj : c) {
            modified |= this.remove(obj);
        }
        return modified;
    }

    @Override
    public String toString() {
        return "HeavyEdgeSet [edges=" + internalSet.toString() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Standard Java semantics: safely compares sizes and elements,
        // evaluating to true for empty sets of different types,
        // while deferring to elements for populated sets.
        return internalSet.equals(o);
    }

    @Override
    public int hashCode() {
        return internalSet.hashCode();
    }
}
