package dev.chpg.pg.api;

import java.util.Collection;

/**
 * An empty, immutable implementation of {@link NodeSet}.
 * <p>
 * <b>What it represents:</b> A singleton representing a mathematical empty set of nodes.
 * <p>
 * <b>Why it exists:</b> To avoid allocating memory for empty node collections, heavily optimizing intersection or filtering operations that yield no results.
 * <p>
 * <b>When to use it:</b> Primarily used internally to return {@link NodeSet#empty()}.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Returning an empty set when a query yields no nodes.</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> Fully thread-safe as it contains no state.
 * <p>
 * <b>Performance characteristics:</b> Zero-allocation singleton. All size/containment checks return in O(1) time.
 */
public final class ImmutableEmptyNodeSet extends AbstractImmutableEmptyElementSet<Node, NodeSet> implements NodeSet {

    @Override
    protected NodeSet emptySet() {
        return NodeSet.empty();
    }

    @Override
    protected NodeSet genericImmutableSet(Collection<? extends Node> elements) {
        return new GenericImmutableNodeSet(elements);
    }
}
