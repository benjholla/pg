package io.github.benjholla.pg.global;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.benjholla.pg.api.AttributeValue;
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public final class GlobalNodeSet implements NodeSet {

    private final HashSet<GlobalNode> internalSet;

    public GlobalNodeSet() {
        this.internalSet = new HashSet<>();
    }

    public GlobalNodeSet(Node initialNode) {
        this();
        add(initialNode);
    }

    public GlobalNodeSet(Node... initialNodes) {
        this();
        Objects.requireNonNull(initialNodes, "Node array cannot be null");
        addAll(Arrays.asList(initialNodes));
    }

    public GlobalNodeSet(Collection<Node> initialNodes) {
        this();
        Objects.requireNonNull(initialNodes, "Node collection cannot be null");
        addAll(initialNodes);
    }

    private GlobalNode validate(Node node) {
        Objects.requireNonNull(node, "Node cannot be null");
        if (!(node instanceof GlobalNode impl)) {
            throw new IllegalArgumentException(
                "Cross-graph contamination: Expected GlobalNode, got " + node.getClass().getSimpleName()
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
        GlobalNodeSet result = new GlobalNodeSet();
        for (GlobalNode node : internalSet) {
           if (node.attributes().containsKey(attribute)) {
                result.internalSet.add(node);
            }
        }
        return new GlobalImmutableNodeSet(result);
    }

    @Override
    public NodeSet filter(String attribute, AttributeValue... values) {
        GlobalNodeSet result = new GlobalNodeSet();
        if (attribute != null && values != null) {
            for (GlobalNode node : internalSet) {
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
        return new GlobalImmutableNodeSet(result);
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        GlobalNodeSet result = new GlobalNodeSet();
        if (other == null || other.isEmpty()) {
            return new GlobalImmutableNodeSet(result);
        }
        for (GlobalNode node : internalSet) {
            if (other.contains(node)) {
                result.internalSet.add(node);
            }
        }
        return new GlobalImmutableNodeSet(result);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        GlobalNodeSet result = new GlobalNodeSet();
        for (GlobalNode node : internalSet) {
            if (other == null || !other.contains(node)) {
                result.internalSet.add(node);
            }
        }
        return new GlobalImmutableNodeSet(result);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        GlobalNodeSet result = new GlobalNodeSet();
        result.internalSet.addAll(this.internalSet);
        if (other != null) {
            for (Node n : other) {
                if (n instanceof GlobalNode gn) {
                    result.internalSet.add(gn);
                }
            }
        }
        return new GlobalImmutableNodeSet(result);
    }

    @Override
    public Set<Integer> ids() {
        return internalSet.stream().map(Node::id).collect(Collectors.toSet());
    }

    @Override
    public int[] toIdArray() {
        return internalSet.stream().mapToInt(Node::id).toArray();
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

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Node> iterator() {
        return (Iterator<Node>) (Iterator<?>) internalSet.iterator();
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
            modified |= internalSet.add((GlobalNode) e);
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<GlobalNode> it = internalSet.iterator();
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
        String joined = internalSet.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
        return "GlobalNodeSet [nodes=" + joined + "]";
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
