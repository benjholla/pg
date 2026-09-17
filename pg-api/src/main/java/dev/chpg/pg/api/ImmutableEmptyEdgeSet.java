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
public final class ImmutableEmptyEdgeSet extends AbstractImmutableEmptyElementSet<Edge, EdgeSet> implements EdgeSet {

    @Override
    protected EdgeSet createImmutableSet(Collection<? extends Edge> elements) {
        return new GenericImmutableEdgeSet(elements);
    }
}
