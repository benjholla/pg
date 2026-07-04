package io.github.benjholla.pg.universe.ephemeral;

import java.util.Collection;
import java.util.Iterator;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class UnmodifiableEphemeralNodeSet implements NodeSet {

    private final Collection<? extends Node> delegate;

    public UnmodifiableEphemeralNodeSet(Collection<? extends Node> delegate) {
        this.delegate = delegate;
    }

    // --- Blocked Mutations (Violent Fail) ---
    @Override
    public boolean add(Node node) { throw new UnsupportedOperationException("Read-only view"); }
    @Override
    public boolean addAll(Collection<? extends Node> c) { throw new UnsupportedOperationException("Read-only view"); }
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
    public java.util.Optional<Node> one() {
        return (java.util.Optional<Node>) (java.util.Optional<?>) delegate.stream().findAny();
    }

    @Override
    public NodeSet filter(String attribute) {
        NodeSet result = new io.github.benjholla.pg.universe.ephemeral.EphemeralNodeSet();
        for (Node node : delegate) {
            if (node.attributes().containsKey(attribute)) {
                result.add(node);
            }
        }
        return result;
    }

    @Override
    public NodeSet filter(String attribute, io.github.benjholla.pg.api.AttributeValue... values) {
        NodeSet result = new io.github.benjholla.pg.universe.ephemeral.EphemeralNodeSet();
        java.util.List<io.github.benjholla.pg.api.AttributeValue> valueList = java.util.Arrays.asList(values);
        for (Node node : delegate) {
            if (node.attributes().containsKey(attribute) && valueList.contains(node.attributes().get(attribute))) {
                result.add(node);
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
    public Iterator<Node> iterator() {
        // Return an unmodifiable iterator to prevent .remove() bypasses
        Iterator<? extends Node> it = delegate.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() { return it.hasNext(); }
            @Override
            public Node next() { return it.next(); }
            @Override
            public void remove() { throw new UnsupportedOperationException("Read-only view"); }
        };
    }
}
