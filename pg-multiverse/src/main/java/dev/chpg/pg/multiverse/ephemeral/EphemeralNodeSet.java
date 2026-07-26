package dev.chpg.pg.multiverse.ephemeral;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import dev.chpg.pg.api.Node;
import dev.chpg.pg.api.NodeSet;

/**
 * A mutable collection for managing distinct {@link EphemeralNode} objects.
 * <p>
 * <b>What it represents:</b> A concrete, materialized implementation of {@code NodeSet} specifically tied to the `pg-multiverse` ephemeral engine.
 * <p>
 * <b>Why it exists:</b> It provides standard HashSet operations while actively enforcing strict domain boundaries to prevent positive-ID nodes (like {@code UniverseNode}) from contaminating the local negative-ID sandbox.
 * <p>
 * <b>When to use it:</b> Use this when manually assembling collections of ephemeral nodes, or as the backing structure for traversals within an ephemeral graph.
 * <p>
 * <b>Common usage patterns:</b> It is used internally to back live views and algebraic computations (union, intersection). Developers can use it directly to construct arbitrary groups of transient nodes.
 * <p>
 * <b>Important invariants:</b> The set actively rejects any non-{@code EphemeralNode} or node with a positive ID during addition, throwing an {@code IllegalArgumentException}. Query operations safely ignore foreign node types.
 * <p>
 * <b>Thread safety:</b> Not thread-safe. Concurrent modifications must be externally synchronized.
 * <p>
 * <b>Performance characteristics:</b> Backed by a standard {@code HashSet}, offering O(1) amortized insertions and lookups, with the standard object-allocation overhead of generic Java collections.
 */
public final class EphemeralNodeSet implements NodeSet {

    private final HashSet<EphemeralNode> internalSet;

    /**
     * Constructs a new, empty {@code EphemeralNodeSet}.
     */
    public EphemeralNodeSet() {
        this.internalSet = new HashSet<>();
    }

    /**
     * Constructs a new {@code EphemeralNodeSet} containing the specified node.
     *
     * @param initialNode the single node to initially add
     */
    public EphemeralNodeSet(Node initialNode) {
        this();
        add(initialNode);
    }

    /**
     * Constructs a new {@code EphemeralNodeSet} containing the specified nodes.
     *
     * @param initialNodes an array of nodes to initially add
     */
    public EphemeralNodeSet(Node... initialNodes) {
        Objects.requireNonNull(initialNodes, "Node array cannot be null");
        this.internalSet = new HashSet<>((int) (initialNodes.length / 0.75f) + 1);
        for (Node node : initialNodes) {
            add(node);
        }
    }

    /**
     * Constructs a new {@code EphemeralNodeSet} containing the elements of the specified collection.
     *
     * @param initialNodes a collection of nodes to initially add
     */
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
        EphemeralGuardrails.requireLocalId(impl.id());
        return impl;
    }

    @Override
    public NodeSet toImmutable() {
        if (internalSet.isEmpty()) { return NodeSet.empty(); }
        if (internalSet.size() == 1) { return new EphemeralImmutableSingletonNodeSet(internalSet.iterator().next()); }
        return new EphemeralImmutableNodeSet(new EphemeralNodeSet(this));
    }

    @Override
    public Optional<Node> one() {
        if (internalSet.isEmpty()) { return Optional.empty(); }
        return Optional.of(internalSet.iterator().next());
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        if (other == null || other.isEmpty()) {
            return result.isEmpty() ? NodeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result));
        }
        for (EphemeralNode node : internalSet) {
            if (other.contains(node)) {
                result.internalSet.add(node);
            }
        }
        return result.isEmpty() ? NodeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result));
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        for (EphemeralNode node : internalSet) {
            if (other == null || !other.contains(node)) {
                result.internalSet.add(node);
            }
        }
        return result.isEmpty() ? NodeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result));
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        EphemeralNodeSet result = new EphemeralNodeSet();
        result.internalSet.addAll(this.internalSet);
        if (other != null) {
            for (Node n : other) {
                if (n instanceof EphemeralNode en) {
                    result.internalSet.add(en);
                }
            }
        }
        return result.isEmpty() ? NodeSet.empty() : (result.size() == 1 ? new EphemeralImmutableSingletonNodeSet((EphemeralNode) result.iterator().next()) : new EphemeralImmutableNodeSet(result));
    }

    @Override
    public Set<Integer> ids() {
        Set<Integer> ids = new HashSet<>((int) (internalSet.size() / 0.75f) + 1);
        for (EphemeralNode node : internalSet) {
            ids.add(node.id());
        }
        return ids;
    }

    @Override
    public int[] toIdArray() {
        int[] result = new int[internalSet.size()];
        int i = 0;
        for (Node node : internalSet) {
            result[i++] = node.id();
        }
        return result;
    }

    @Override
    public boolean add(Node node) {
        return internalSet.add(validate(node));
    }

    @Override
    public boolean contains(Object obj) {
        if (!(obj instanceof Node node)) { return false; }
        try {
            return internalSet.contains(validate(node));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean remove(Object obj) {
        if (!(obj instanceof Node node)) { return false; }
        try {
            return internalSet.remove(validate(node));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    @Override
public boolean isMaterialized() {
        return true;
    }

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
        String joined = internalSet.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
        return "EphemeralNodeSet [nodes=" + joined + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
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
