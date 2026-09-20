package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A generic, immutable implementation of {@link EdgeSet}.
 * <p>
 * <b>What it represents:</b> An unmodifiable, materialized collection of edges.
 * <p>
 * <b>Why it exists:</b> To provide a guaranteed safe snapshot of edges that cannot be altered, ensuring query results remain stable even if the underlying graph mutates.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return materialized results from operations like {@link EdgeSet#materialize()}.
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
public final class GenericImmutableEdgeSet extends AbstractGenericImmutableElementSet<Edge> implements EdgeSet {

    /**
     * Constructs a new generic immutable edge set from the provided elements.
     *
     * @param elements the collection of edges
     */
    public GenericImmutableEdgeSet(Collection<? extends Edge> elements) {
        super(elements);
    }

    @Override
    public EdgeSet materialize() {
        return this;
    }

    @Override
    public EdgeSet toImmutable() {
        return this;
    }

    @Override
    public EdgeSet intersect(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return EdgeSet.empty();
        }
        Set<Edge> intersected = new HashSet<>();
        for (Edge e : elements) {
            if (other.contains(e)) {
                intersected.add(e);
            }
        }
        return intersected.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(Collections.unmodifiableSet(intersected));
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Edge> differenced = new HashSet<>();
        for (Edge e : elements) {
            if (!other.contains(e)) {
                differenced.add(e);
            }
        }
        return differenced.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(Collections.unmodifiableSet(differenced));
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        Set<Edge> unioned = new HashSet<>((int) ((elements.size() + other.size()) / 0.75f) + 1);
        unioned.addAll(elements);
        unioned.addAll(other);
        return new GenericImmutableEdgeSet(Collections.unmodifiableSet(unioned));
    }
}
