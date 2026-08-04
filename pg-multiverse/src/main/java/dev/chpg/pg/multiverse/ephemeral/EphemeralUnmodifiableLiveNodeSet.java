package dev.chpg.pg.multiverse.ephemeral;

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

import dev.chpg.pg.api.Edge;
import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

/**
 * A live, unmodifiable view of nodes within an {@code EphemeralGraph}.
 * <p>
 * <b>What it represents:</b> A read-only projection of the temporary sandbox's internal node registry.
 * <p>
 * <b>Why it exists:</b> It exposes the dynamic, breathing topology of an ephemeral graph while strictly rejecting external mutation attempts, preventing the graph's invariants from being compromised.
 * <p>
 * <b>When to use it:</b> It is used internally to safely return the results of queries like {@code graph.nodes()}.
 * <p>
 * <b>Common usage patterns:</b> Iterate over the view directly, or convert it to an immutable snapshot (via {@code toImmutable()}) if a persistent record is required.
 * <p>
 * <b>Important invariants:</b> Direct modifications via {@code add()} or {@code remove()} throw an {@code UnsupportedOperationException}. It perfectly mirrors real-time additions and removals from the underlying ephemeral graph.
 * <p>
 * <b>Thread safety:</b> Not inherently thread-safe. Changes to the underlying graph while iterating will cause undefined behavior unless synchronized.
 * <p>
 * <b>Performance characteristics:</b> Operations like {@code size()} and {@code contains()} are resolved in O(1) time directly against the live engine maps.
 */
public class EphemeralUnmodifiableLiveNodeSet implements NodeSet {

    private final Map<Integer, Node> nodes;

    /**
     * Constructs a new unmodifiable live node set backing the ephemeral graph view.
     *
     * @param nodes    the core ephemeral node registry map
     * @param edges    the ephemeral edge registry map (unused in node sets directly)
     * @param inEdges  the inbound ephemeral adjacency map
     * @param outEdges the outbound ephemeral adjacency map
     */
    public EphemeralUnmodifiableLiveNodeSet(
            Map<Integer, Node> nodes,
            Map<Integer, Edge> edges,
            Map<Integer, EphemeralEdgeSet> inEdges,
            Map<Integer, EphemeralEdgeSet> outEdges) {
        this.nodes = Objects.requireNonNull(nodes);
    }

    @Override
    public NodeSet toImmutable() {
        if (nodes.isEmpty()) { return NodeSet.empty(); }
        if (nodes.size() == 1) { return new EphemeralImmutableSingletonNodeSet(nodes.values().iterator().next()); }
        return new EphemeralImmutableNodeSet(new EphemeralNodeSet(nodes.values()));
    }

    @Override
    public Optional<Node> one() {
        if (nodes.isEmpty()) { return Optional.empty(); }
        return Optional.of(nodes.values().iterator().next());
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "other cannot be null");
        // Delegate directly to the secured, fail-fast algebra in EphemeralNodeSet
        return new EphemeralNodeSet(nodes.values()).intersect(other);
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "other cannot be null");
        // Delegate directly to the secured, fail-fast algebra in EphemeralNodeSet
        return new EphemeralNodeSet(nodes.values()).difference(other);
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        Objects.requireNonNull(other, "other cannot be null");
        // Delegate directly to the secured, fail-fast algebra in EphemeralNodeSet
        return new EphemeralNodeSet(nodes.values()).union(other);
    }

    @Override
    public Set<Integer> ids() {
        return java.util.Collections.unmodifiableSet(nodes.keySet());
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[nodes.size()];
        int i = 0;
        for (Integer id : nodes.keySet()) {
            result[i++] = id;
        }
        return result;
    }

    @Override
    public boolean add(Node node) {
        throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
    }

    @Override
    public boolean contains(Object obj) {
        // WIDENED: Accept the broader Node interface to allow Shadow lookups
        if (!(obj instanceof Node gn)) { return false; }
        Node found = nodes.get(gn.id());
        return found != null && found.equals(gn);
    }

    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    public int size() {
        return nodes.size();
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
    }

    @Override
    public Iterator<Node> iterator() {
        // Preserve anonymous wrapper: Prevents iterator.remove() from bypassing graph mutation invariants or immutability contracts.
        return new Iterator<Node>() {
            private final Iterator<Node> it = nodes.values().iterator();
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
                throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
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
        throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
    }

    @Override
    public void forEach(Consumer<? super Node> action) {
        nodes.values().forEach(action);
    }

    @Override
    public Spliterator<Node> spliterator() {
        return nodes.values().spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return nodes.values().toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super Node> filter) {
        throw new UnsupportedOperationException("Live views are strictly unmodifiable.");
    }

    @Override
    public Stream<Node> stream() {
        return nodes.values().stream();
    }

    @Override
    public Stream<Node> parallelStream() {
        return nodes.values().parallelStream();
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
        for (Node node : nodes.values()) {
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
}
