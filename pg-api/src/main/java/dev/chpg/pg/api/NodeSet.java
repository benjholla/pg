package dev.chpg.pg.api;

import java.util.Collection;

/**
 * A specialized set for managing collections of {@link Node}s.
 * <p>
 * <b>What it represents:</b> A distinct collection of graph nodes, supporting set-theoretic math and fluent filtering.
 * <p>
 * <b>Why it exists:</b> To provide a zero-allocation, functional query pipeline for graph analysis, enabling operations like filtering without intermediate collection allocation.
 * <p>
 * <b>When to use it:</b> Use {@code NodeSet} whenever dealing with aggregate collections of nodes, especially when extracting properties or executing declarative filters.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Filtering nodes via fluent predicates (e.g., {@code nodes.withAnyTag("Person")}).</li>
 * <li>Extracting primitive ID arrays for bulk operations (e.g., {@code nodes.toIdArray()}).</li>
 * <li>Forcing eager materialization of deferred pipelines via {@code materialize()}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> {@code NodeSet} implementations may be "live" (dynamically reflecting changes in the underlying graph) or "snapshot" based. Many filtering methods return a deferred evaluation pipeline that only executes when terminal operations (like {@code size()} or {@code toIdArray()}) are invoked.
 * <p>
 * <b>Thread safety:</b> Thread safety guarantees depend heavily on the concrete implementation. Modifying the underlying graph while iterating a live NodeSet will likely produce a {@code ConcurrentModificationException}.
 * <p>
 * <b>Performance characteristics:</b> The fluent filtering API (e.g., {@code withAttribute}) returns a deferred, zero-allocation wrapper. Terminal operations on deferred sets evaluate the pipeline and take O(N) time.
 */
public interface NodeSet extends ElementSet<Node> {

    /**
     * An immutable, empty node set singleton.
     */
    NodeSet EMPTY = new ImmutableEmptyNodeSet();

    /**
     * Returns an empty node set.
     *
     * @return an empty NodeSet
     */
    static NodeSet empty() {
        return EMPTY;
    }

    @Override
    NodeSet toImmutable();

    @Override
    default NodeSet withAttribute(String attribute) {
        return new DeferredNodeSet(this, n -> n.attributes().containsKey(attribute));
    }

    @Override
    default NodeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredNodeSet(this, n -> {
            AttributeValue val = n.attributes().get(attribute);
            if (val == null || values == null || values.length == 0) { return false; }
            for (AttributeValue v : values) {
                if (java.util.Objects.equals(val, v)) { return true; }
            }
            return false;
        });
    }

    @Override
    default NodeSet withAnyTag(String... tags) {
        return new DeferredNodeSet(this, n -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (n.tags().contains(tag)) { return true; }
            }
            return false;
        });
    }

    @Override
    default NodeSet withAllTags(String... tags) {
        return new DeferredNodeSet(this, n -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (!n.tags().contains(tag)) { return false; }
            }
            return true;
        });
    }

    @Override
    default NodeSet materialize() {
        java.util.Set<Node> materialized = new java.util.HashSet<>();
        for (Node n : this) {
            materialized.add(n);
        }
        return materialized.isEmpty() ? NodeSet.empty() : new GenericImmutableNodeSet(java.util.Collections.unmodifiableSet(materialized));
    }

    @Override
    NodeSet intersect(Collection<? extends Node> other);

    @Override
    NodeSet difference(Collection<? extends Node> other);

    @Override
    NodeSet union(Collection<? extends Node> other);
}
