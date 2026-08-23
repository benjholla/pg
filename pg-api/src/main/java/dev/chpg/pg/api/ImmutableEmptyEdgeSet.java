package dev.chpg.pg.api;

import java.util.Collection;

/**
 * An empty, immutable implementation of {@link EdgeSet}.
 * <p>
 * <b>What it represents:</b> A singleton representing a mathematical empty set of edges.
 * <p>
 * <b>Why it exists:</b> To avoid allocating memory for empty edge collections, heavily optimizing intersection or filtering operations that yield no results.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return {@link EdgeSet#empty()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Returning an empty set when a query yields no edges.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe as it contains no state.
 * <p>
 * <b>Performance characteristics:</b> Zero-allocation singleton. All size/containment checks return in O(1) time.
 */
public final class ImmutableEmptyEdgeSet extends AbstractImmutableEmptyElementSet<Edge, EdgeSet> implements EdgeSet {

    @Override
    protected EdgeSet emptySet() {
        return EdgeSet.empty();
    }

    @Override
    protected EdgeSet genericImmutableSet(Collection<? extends Edge> elements) {
        return new GenericImmutableEdgeSet(elements);
    }
}
