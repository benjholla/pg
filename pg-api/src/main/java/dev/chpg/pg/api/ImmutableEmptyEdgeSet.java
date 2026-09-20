package dev.chpg.pg.api;

import java.util.Collection;

/**
 * An empty, immutable implementation of {@link EdgeSet}.
 * <p>
 * <b>What it represents:</b> A singleton representing a mathematical empty set of edges.
 * <p>
 * <b>Why it exists:</b> To prevent unnecessary memory allocations when returning empty results from graph queries.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return {@link EdgeSet#empty()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Returning an empty set when a query yields no edges.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe as it is an empty, immutable singleton.
 * <p>
 * <b>Performance characteristics:</b> Zero allocation overhead, O(1) for all operations.
 */
public final class ImmutableEmptyEdgeSet extends AbstractImmutableEmptyElementSet<Edge> implements EdgeSet {

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
        return this;
    }

    @Override
    public EdgeSet difference(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        return this;
    }

    @Override
    public EdgeSet union(Collection<? extends Edge> other) {
        java.util.Objects.requireNonNull(other, "other cannot be null");
        if (other.isEmpty()) {
            return this;
        }
        if (other instanceof EdgeSet) {
            return ((EdgeSet) other).toImmutable();
        }
        return new GenericImmutableEdgeSet(other);
    }
}
