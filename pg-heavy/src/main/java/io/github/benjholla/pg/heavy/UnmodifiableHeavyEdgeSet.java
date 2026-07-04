package io.github.benjholla.pg.heavy;

import java.util.Collection;
import java.util.Iterator;
import io.github.benjholla.pg.api.Edge;
import io.github.benjholla.pg.api.EdgeSet;

public class UnmodifiableHeavyEdgeSet implements EdgeSet {

    private final Collection<? extends Edge> delegate;

    public UnmodifiableHeavyEdgeSet(Collection<? extends Edge> delegate) {
        this.delegate = delegate;
    }

    // --- Blocked Mutations (Violent Fail) ---
    @Override
    public boolean add(Edge edge) { throw new UnsupportedOperationException("Read-only view"); }
    @Override
    public boolean addAll(Collection<? extends Edge> c) { throw new UnsupportedOperationException("Read-only view"); }
    @Override
    public boolean remove(Object o) { throw new UnsupportedOperationException("Read-only view"); }
    @Override
    public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException("Read-only view"); }
    @Override
    public void clear() { throw new UnsupportedOperationException("Read-only view"); }

    @Override
    public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException("Read-only view"); }

    // --- Safe Delegated Reads (Silent Ignore / Standard Semantics) ---
    @Override
    public boolean contains(Object o) { return delegate.contains(o); }
    @Override
    public int size() { return delegate.size(); }
    @Override
    public boolean isEmpty() { return delegate.isEmpty(); }

    @Override
    public java.util.Optional<Edge> one() {
        return (java.util.Optional<Edge>) (java.util.Optional<?>) delegate.stream().findAny();
    }

    @Override
    public EdgeSet filter(String attribute) {
        EdgeSet result = new io.github.benjholla.pg.heavy.HeavyEdgeSet();
        for (Edge edge : delegate) {
            if (edge.attributes().containsKey(attribute)) {
                result.add(edge);
            }
        }
        return result;
    }

    @Override
    public EdgeSet filter(String attribute, io.github.benjholla.pg.api.AttributeValue... values) {
        EdgeSet result = new io.github.benjholla.pg.heavy.HeavyEdgeSet();
        java.util.List<io.github.benjholla.pg.api.AttributeValue> valueList = java.util.Arrays.asList(values);
        for (Edge edge : delegate) {
            if (edge.attributes().containsKey(attribute) && valueList.contains(edge.attributes().get(attribute))) {
                result.add(edge);
            }
        }
        return result;
    }

    @Override
    public boolean containsAll(Collection<?> c) { return delegate.containsAll(c); }

    @Override
    public Object[] toArray() { return delegate.toArray(); }

    @Override
    public <T> T[] toArray(T[] a) { return delegate.toArray(a); }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Edge> iterator() {
        // Return an unmodifiable iterator to prevent .remove() bypasses
        Iterator<? extends Edge> it = delegate.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() { return it.hasNext(); }
            @Override
            public Edge next() { return it.next(); }
            @Override
            public void remove() { throw new UnsupportedOperationException("Read-only view"); }
        };
    }
}
