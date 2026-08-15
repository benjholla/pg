package dev.chpg.pg.global;

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
import dev.chpg.pg.api.EdgeSet;

/**
 * A live, unmodifiable view of edges within a {@link GlobalGraph}.
 * <p>
 * <b>What it represents:</b> A read-only projection of the graph's internal adjacency edges.
 * <p>
 * <b>Why it exists:</b> It provides a dynamic window into the structural state of the graph without exposing the underlying primitive arrays or risking external corruption.
 * <p>
 * <b>When to use it:</b> It is used internally by the core graph engine to satisfy traversal queries (like {@code forwardStep()}) securely.
 * <p>
 * <b>Common usage patterns:</b> Users iterate through this set or spawn immutable snapshots from it to begin processing an edge collection.
 * <p>
 * <b>Important invariants:</b> Attempts to modify this set via {@code add()} or {@code remove()} will throw an {@code UnsupportedOperationException}. However, structural changes to the parent graph will instantly reflect in this view.
 * <p>
 * <b>Thread safety:</b> Not inherently thread-safe if the underlying graph is concurrently structurally modified.
 * <p>
 * <b>Performance characteristics:</b> Operations like {@code size()} and {@code contains()} are executed in O(1) time against the live engine state. Iteration avoids full instantiation but cannot be heavily optimized.
 */
public class GlobalUnmodifiableLiveEdgeSet implements EdgeSet {

    private final Map<Integer, GlobalEdge> edges;

    /**
     * Constructs a new unmodifiable live edge set backing the global graph view.
     *
     * @param nodes    the node registry map (unused in edge sets directly)
     * @param edges    the core edge registry map
     * @param inEdges  the inbound adjacency map
     * @param outEdges the outbound adjacency map
     */
    public GlobalUnmodifiableLiveEdgeSet(
            Map<Integer, GlobalNode> nodes,
            Map<Integer, GlobalEdge> edges,
            Map<Integer, GlobalEdgeSet> inEdges,
            Map<Integer, GlobalEdgeSet> outEdges) {
        this.edges = Objects.requireNonNull(edges);
    }

    @Override
    public EdgeSet toImmutable() {
        if (edges.isEmpty()) { return EdgeSet.empty(); }
        if (edges.size() == 1) { return new GlobalImmutableSingletonEdgeSet(edges.values().iterator().next()); }
        GlobalEdgeSet copy = new GlobalEdgeSet();
        copy.addAll(edges.values());
        return copy.asSealed();
    }

    @Override
    public Optional<Edge> one() {
        if (edges.isEmpty()) { return Optional.empty(); }
        return Optional.of(edges.values().iterator().next());
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        GlobalEdgeSet result = new GlobalEdgeSet();
        if (other.isEmpty()) {
            return result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed();
        }
        for (GlobalEdge edge : edges.values()) {
            if (other.contains(edge)) {
                result.add(edge);
            }
        }
        return result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed();
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        GlobalEdgeSet result = new GlobalEdgeSet();
        for (GlobalEdge edge : edges.values()) {
            if (!other.contains(edge)) {
                result.add(edge);
            }
        }
        return result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed();
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        GlobalEdgeSet result = new GlobalEdgeSet(); result.addAll(edges.values());
        for (Edge e : other) {
            if (e instanceof GlobalEdge) {
                result.add(e);
            }
        }
        return result.size() == 1 ? new GlobalImmutableSingletonEdgeSet((GlobalEdge) result.iterator().next()) : result.asSealed();
    }

    @Override
    public Set<Integer> ids() {
        return java.util.Collections.unmodifiableSet(edges.keySet());
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[edges.size()];
        int i = 0;
        for (Integer id : edges.keySet()) {
            result[i++] = id;
        }
        return result;
    }

    @Override
    public boolean add(Edge edge) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof GlobalEdge ge)) { return false; }
        return edges.containsKey(ge.id());
    }

    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException();
    }
    @Override
public boolean isMaterialized() {
        return true;
    }

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
        // Preserve anonymous wrapper: Prevents iterator.remove() from bypassing graph mutation invariants or immutability contracts.
        return new Iterator<Edge>() {
            private final Iterator<GlobalEdge> it = edges.values().iterator();
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
        for (GlobalEdge edge : edges.values()) {
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
