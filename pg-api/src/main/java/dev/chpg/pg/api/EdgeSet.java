package dev.chpg.pg.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * A specialized set for managing collections of {@link Edge}s.
 * <p>
 * <b>What it represents:</b> A distinct collection of graph edges, supporting set-theoretic math and fluent filtering.
 * <p>
 * <b>Why it exists:</b> To provide a zero-allocation, functional query pipeline for graph analysis, enabling operations like filtering without intermediate collection allocation.
 * <p>
 * <b>When to use it:</b> Use {@code EdgeSet} whenever dealing with aggregate collections of edges, especially when extracting properties or executing declarative filters.
 * <p>
 * <b>Common usage patterns:</b>
 * <ul>
 * <li>Filtering edges via fluent predicates (e.g., {@code edges.withAnyTag("knows")}).</li>
 * <li>Extracting primitive ID arrays for bulk operations (e.g., {@code edges.toIdArray()}).</li>
 * <li>Forcing eager materialization of deferred pipelines via {@code materialize()}.</li>
 * </ul>
 * <p>
 * <b>Important invariants:</b> {@code EdgeSet} implementations may be "live" (dynamically reflecting changes in the underlying graph) or "snapshot" based. Many filtering methods return a deferred evaluation pipeline that only executes when terminal operations (like {@code size()} or {@code toIdArray()}) are invoked.
 * <p>
 * <b>Thread safety:</b> Thread safety guarantees depend heavily on the concrete implementation. Modifying the underlying graph while iterating a live EdgeSet will likely produce a {@code ConcurrentModificationException}.
 * <p>
 * <b>Performance characteristics:</b> The fluent filtering API (e.g., {@code withAttribute}) returns a deferred, zero-allocation wrapper. Terminal operations on deferred sets evaluate the pipeline and take O(N) time.
 */
public interface EdgeSet extends ElementSet<Edge> {

    /**
     * An immutable, empty edge set singleton.
     */
    EdgeSet EMPTY = new ImmutableEmptyEdgeSet();

    /**
     * Returns an empty edge set.
     *
     * @return an empty EdgeSet
     */
    static EdgeSet empty() {
        return EMPTY;
    }

    /**
     * Converts this edge set into an immutable snapshot.
     *
     * @return an immutable EdgeSet
     */
    @Override
    EdgeSet toImmutable();

    @Override
    default EdgeSet withAttribute(String attribute) {
        return new DeferredEdgeSet(this, e -> e.attributes().containsKey(attribute));
    }

    @Override
    default EdgeSet withAttribute(String attribute, AttributeValue... values) {
        return new DeferredEdgeSet(this, e -> {
            AttributeValue val = e.attributes().get(attribute);
            if (val == null || values == null || values.length == 0) { return false; }
            for (AttributeValue v : values) {
                if (java.util.Objects.equals(val, v)) { return true; }
            }
            return false;
        });
    }

    @Override
    default EdgeSet withAnyTag(String... tags) {
        return new DeferredEdgeSet(this, e -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (e.tags().contains(tag)) { return true; }
            }
            return false;
        });
    }

    @Override
    default EdgeSet withAllTags(String... tags) {
        return new DeferredEdgeSet(this, e -> {
            if (tags == null || tags.length == 0) { return false; }
            for (String tag : tags) {
                if (!e.tags().contains(tag)) { return false; }
            }
            return true;
        });
    }

    @Override
    default EdgeSet materialize() {
        Set<Edge> materialized = new java.util.HashSet<>();
        for (Edge e : this) {
            materialized.add(e);
        }
        return materialized.isEmpty() ? EdgeSet.empty() : new GenericImmutableEdgeSet(java.util.Collections.unmodifiableSet(materialized));
    }

    @Override
    EdgeSet intersect(Collection<? extends Edge> other);

    @Override
    EdgeSet difference(Collection<? extends Edge> other);

    @Override
    EdgeSet union(Collection<? extends Edge> other);
}
