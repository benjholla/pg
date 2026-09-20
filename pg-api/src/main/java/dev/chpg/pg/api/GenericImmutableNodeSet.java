package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A generic, immutable implementation of {@link NodeSet}.
 * <p>
 * <b>What it represents:</b> An unmodifiable, materialized collection of nodes.
 * <p>
 * <b>Why it exists:</b> To provide a guaranteed safe snapshot of nodes that cannot be altered, ensuring query results remain stable even if the underlying graph mutates.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return materialized results from operations like {@link NodeSet#materialize()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Caching stable query results for repeated analysis.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe for reading because the internal state is fundamentally unmodifiable.
 * <p>
 * <b>Performance characteristics:</b> Requires O(N) memory allocation to materialize the underlying objects, but provides fast O(1) size checks and O(1) containment checks.
 */
public final class GenericImmutableNodeSet extends AbstractGenericImmutableElementSet<Node> implements NodeSet {

    /**
     * Constructs a new generic immutable node set from the provided elements.
     *
     * @param elements the collection of nodes
     */
    public GenericImmutableNodeSet(Collection<? extends Node> elements) {
        super(elements);
    }

    @Override
    public NodeSet materialize() {
        return this;
    }

    @Override
    public NodeSet toImmutable() {
        return this;
    }

    @Override
    public NodeSet intersect(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return NodeSet.empty();
        }
        Set<Node> intersected = new HashSet<>();
        for (Node n : elements) {
            if (other.contains(n)) {
                intersected.add(n);
            }
        }
        return intersected.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(Collections.unmodifiableSet(intersected));
    }

    @Override
    public NodeSet difference(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Node> differenced = new HashSet<>();
        for (Node n : elements) {
            if (!other.contains(n)) {
                differenced.add(n);
            }
        }
        return differenced.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(Collections.unmodifiableSet(differenced));
    }

    @Override
    public NodeSet union(Collection<? extends Node> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Node> unioned = new HashSet<>((int) ((elements.size() + other.size()) / 0.75f) + 1);
        unioned.addAll(elements);
        unioned.addAll(other);
        return new GenericImmutableNodeSet(Collections.unmodifiableSet(unioned));
    }
}
