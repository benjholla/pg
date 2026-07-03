package io.github.benjholla.pg.universe.ephemeral;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class EphemeralNodeSet implements NodeSet {

    private final HashSet<EphemeralNode> internalSet;

    public EphemeralNodeSet() {
        this.internalSet = new HashSet<>();
    }

    public EphemeralNodeSet(Node initialNode) {
        this();
        add(initialNode);
    }

    public EphemeralNodeSet(Node... initialNodes) {
        this();
        Objects.requireNonNull(initialNodes, "Node array cannot be null");
        addAll(Arrays.asList(initialNodes));
    }

    public EphemeralNodeSet(Collection<Node> initialNodes) {
        this();
        Objects.requireNonNull(initialNodes, "Node collection cannot be null");
        addAll(initialNodes);
    }

    private EphemeralNode validate(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (!(node instanceof EphemeralNode impl)) {
            throw new IllegalArgumentException(
                "Cross-graph contamination: Expected EphemeralNode, got " + node.getClass().getSimpleName()
            );
        }
        return impl;
    }

    @Override
    public Optional<Node> one() {
        return internalSet.stream().map(e -> (Node) e).findAny();
    }

    @Override
    public NodeSet filter(String attribute) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        for (EphemeralNode node : internalSet) {
           if (node.attributes().containsKey(attribute)) {
                result.internalSet.add(node);
            }
        }
        return result;
    }

    @Override
    public NodeSet filter(String attribute, AttributeValue... values) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        if (attribute != null && values != null) {
            for (EphemeralNode node : internalSet) {
               AttributeValue attributeValue = node.attributes().get(attribute);
                if (attributeValue != null) {
                    for (AttributeValue value : values) {
                        if (value != null) {
                            if (Objects.equals(attributeValue, value)) {
                                result.internalSet.add(node);
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
    public boolean add(Node node) {
        return internalSet.add(validate(node));
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Node node)) return false;
        try {
            return internalSet.contains(validate(node));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Node node)) return false;
        try {
            return internalSet.remove(validate(node));
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
    public Iterator<Node> iterator() {
        Iterator<EphemeralNode> internalIterator = internalSet.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() { return internalIterator.hasNext(); }
            @Override
            public Node next() { return internalIterator.next(); }
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
    public boolean addAll(Collection<? extends Node> c) {
        Objects.requireNonNull(c, "Node collection cannot be null");
        for (Node e : c) {
            validate(e);
        }
        boolean modified = false;
        for (Node e : c) {
            modified |= internalSet.add((EphemeralNode) e);
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<EphemeralNode> it = internalSet.iterator();
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
        return "EphemeralNodeSet [nodes=" + internalSet.toString() + "]";
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
