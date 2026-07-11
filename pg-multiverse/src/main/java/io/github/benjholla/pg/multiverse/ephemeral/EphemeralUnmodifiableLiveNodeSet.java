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
import io.github.benjholla.pg.api.Node;
import io.github.benjholla.pg.api.NodeSet;

public class EphemeralUnmodifiableLiveNodeSet implements NodeSet {

    private final Map<Integer, EphemeralNode> nodes;

    public EphemeralUnmodifiableLiveNodeSet(
            Map<Integer, EphemeralNode> nodes,
            Map<Integer, EphemeralEdge> edges,
            Map<Integer, EphemeralEdgeSet> inEdges,
            Map<Integer, EphemeralEdgeSet> outEdges) {
        this.nodes = Objects.requireNonNull(nodes);
    }

    @Override
    public NodeSet toImmutable() {
        if (nodes.isEmpty()) return NodeSet.empty();
        if (nodes.size() == 1) return new EphemeralImmutableSingletonNodeSet(nodes.values().iterator().next());
        EphemeralNodeSet copy = new EphemeralNodeSet();
        copy.addAll(nodes.values());
        return new EphemeralImmutableNodeSet(copy);
    }

    @Override
    public Optional<Node> one() {
        return nodes.values().stream().map(n -> (Node) n).findAny();
    }

    @Override
    public NodeSet filter(String attribute) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        for (EphemeralNode node : nodes.values()) {
            if (node.attributes().containsKey(attribute)) {
                result.add(node);
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result);
    }

    @Override
    public NodeSet filter(String attribute, AttributeValue... values) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        if (attribute != null && values != null) {
            for (EphemeralNode node : nodes.values()) {
                AttributeValue attributeValue = node.attributes().get(attribute);
                if (attributeValue != null) {
                    for (AttributeValue value : values) {
                        if (value != null) {
                            if (Objects.equals(attributeValue, value)) {
                                result.add(node);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result);
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        if (other == null || other.isEmpty()) {
            return result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result);
        }
        for (EphemeralNode node : nodes.values()) {
            if (other.contains(node)) {
                result.add(node);
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        for (EphemeralNode node : nodes.values()) {
            if (other == null || !other.contains(node)) {
                result.add(node);
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        EphemeralNodeSet result = new EphemeralNodeSet(); result.addAll(nodes.values());
        if (other != null) {
            for (Node n : other) {
                if (n instanceof EphemeralNode) {
                    result.add(n);
                }
            }
        }
        return result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result);
    }

    @Override
    public Set<Integer> ids() {
        return java.util.Collections.unmodifiableSet(nodes.keySet());
    }

    @Override
    public int[] toIdArray() {
        return nodes.keySet().stream().mapToInt(Integer::intValue).toArray();
    }

    @Override
    public boolean add(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof EphemeralNode gn)) return false;
        return nodes.containsKey(gn.id()) && nodes.get(gn.id()).equals(gn);
    }

    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Node> iterator() {
        // Preserve anonymous wrapper: Prevents iterator.remove() from bypassing graph mutation invariants or immutability contracts.
        return new Iterator<Node>() {
            private final Iterator<EphemeralNode> it = nodes.values().iterator();
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

    @Override
    public Object[] toArray() {
        return nodes.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return nodes.values().toArray(a);
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
    public void forEach(Consumer<? super Node> action) {
        nodes.values().forEach(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<Node> spliterator() {
        return (Spliterator<Node>) (Spliterator<?>) nodes.values().spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return nodes.values().toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super Node> filter) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<Node> stream() {
        return (Stream<Node>) (Stream<?>) nodes.values().stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<Node> parallelStream() {
        return (Stream<Node>) (Stream<?>) nodes.values().parallelStream();
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
        for (EphemeralNode node : nodes.values()) {
            if (node != null) {
                h += node.hashCode();
            }
        }
        return h;
    }

    @Override
    public String toString() {
        return nodes.values().toString();
    }

    @Override
    public NodeSet taggedWithAny(String... tags) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        if (tags != null && tags.length > 0) {
            for (Node e : this) {
                for (String tag : tags) {
                    if (e.tags().contains(tag)) {
                        result.add(e);
                        break;
                    }
                }
            }
        }
        return result.isEmpty() ? NodeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result));
    }

    @Override
    public NodeSet taggedWithAll(String... tags) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        if (tags != null && tags.length > 0) {
            for (Node e : this) {
                boolean add = true;
                for (String tag : tags) {
                    if (!e.tags().contains(tag)) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    result.add(e);
                }
            }
        }
        return result.isEmpty() ? NodeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result));
    }
}
